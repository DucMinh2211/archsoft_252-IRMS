package com.irms.order.service;

import com.irms.order.domain.Order;
import com.irms.order.domain.OrderItem;
import com.irms.order.domain.OrderItemStatus;
import com.irms.order.domain.OrderStatus;
import com.irms.order.domain.OrderType;
import com.irms.order.dto.OrderItemRequestDTO;
import com.irms.order.dto.OrderRequestDTO;
import com.irms.order.dto.OrderResponseDTO;
import com.irms.order.dto.TableResponseDTO;
import com.irms.order.exception.BusinessValidationException;
import com.irms.order.exception.OrderAlreadyCancelledException;
import com.irms.order.exception.OrderNotFoundException;
import com.irms.order.infrastructure.client.TableServiceClient;
import com.irms.order.mapper.OrderMapper;
import com.irms.order.repository.OrderRepository;
import com.irms.order.validator.OrderStateValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final TableServiceClient tableServiceClient;
    private final OrderItemFactory orderItemFactory;
    private final OrderTotalCalculator orderTotalCalculator;
    private final KitchenTicketDispatcher kitchenTicketDispatcher;
    private final OrderStateValidator stateValidator;
    private final OrderEventPublisher eventPublisher;

    @PersistenceContext
    private EntityManager entityManager;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderMapper orderMapper,
                            TableServiceClient tableServiceClient,
                             OrderItemFactory orderItemFactory,
                             OrderTotalCalculator orderTotalCalculator,
                             KitchenTicketDispatcher kitchenTicketDispatcher,
                             OrderStateValidator stateValidator,
                             OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.tableServiceClient = tableServiceClient;
        this.orderItemFactory = orderItemFactory;
        this.orderTotalCalculator = orderTotalCalculator;
        this.kitchenTicketDispatcher = kitchenTicketDispatcher;
        this.stateValidator = stateValidator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        // Validate table if Dine In
        if (requestDTO.getType() == OrderType.DINE_IN) {
            if (requestDTO.getTableId() == null) {
                throw new BusinessValidationException("Table ID is required for DINE_IN orders.");
            }
            TableResponseDTO table = tableServiceClient.getTable(requestDTO.getTableId());
            if (!"AVAILABLE".equals(table.getStatus()) && !"OCCUPIED".equals(table.getStatus())) {
                // Warning or error depending on strictness. Let's assume OCCUPIED is fine for adding an order.
            }
        }

        Order order = new Order();
        order.setTableId(requestDTO.getTableId());
        order.setWaiterId(requestDTO.getWaiterId());
        order.setType(requestDTO.getType());
        order.setSpecialNote(requestDTO.getSpecialNote());
        order.setStatus(OrderStatus.DRAFT);
        
        if (requestDTO.getItems() != null && !requestDTO.getItems().isEmpty()) {
            for (OrderItemRequestDTO itemDTO : requestDTO.getItems()) {
                OrderItem orderItem = orderItemFactory.create(itemDTO);
                order.addItem(orderItem);
            }
        }

        orderTotalCalculator.recalculate(order);
        
        Order savedOrder = orderRepository.save(order);
        OrderResponseDTO dto = orderMapper.toDto(savedOrder);
        eventPublisher.broadcast("order.created", dto);
        return dto;
    }

    @Override
    public OrderResponseDTO getOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        return orderMapper.toDto(order);
    }

    @Override
    public Page<OrderResponseDTO> getOrders(OrderStatus status, UUID waiterId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (waiterId != null) {
                predicates.add(cb.equal(root.get("waiterId"), waiterId));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return orderRepository.findAll(spec, pageable).map(orderMapper::toDto);
    }

    @Override
    public List<OrderResponseDTO> getKitchenOrders() {
        List<Order> kitchenOrders = orderRepository.findByStatusIn(List.of(OrderStatus.PENDING, OrderStatus.COOKING));
        return kitchenOrders.stream().map(orderMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(UUID id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderAlreadyCancelledException("Cannot update a cancelled order.");
        }

        stateValidator.validateTransition(order.getStatus(), newStatus);
        
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        
        // If order is cancelled, we might want to cancel all pending items
        if (newStatus == OrderStatus.CANCELLED) {
            order.getItems().forEach(item -> {
                if (item.getStatus() == OrderItemStatus.PENDING) {
                    item.setStatus(OrderItemStatus.CANCELLED);
                }
            });
        }
        
        if (oldStatus == OrderStatus.DRAFT && newStatus == OrderStatus.PENDING) {
            kitchenTicketDispatcher.dispatch(order);
        }

        OrderResponseDTO dto = orderMapper.toDto(orderRepository.save(order));
        eventPublisher.broadcast("order.status", dto);
        return dto;
    }

    @Override
    @Transactional
    public void deleteOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        if (order.getStatus() != OrderStatus.DRAFT && order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessValidationException("Cannot delete order that is already being processed or completed.");
        }

        orderRepository.delete(order);
        eventPublisher.broadcast("order.deleted", id);
    }

    @Override
    @Transactional
    public OrderResponseDTO addOrderItem(UUID orderId, OrderItemRequestDTO itemDTO) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessValidationException("Cannot add items to a completed or cancelled order.");
        }

        OrderItem orderItem = orderItemFactory.create(itemDTO);
        order.addItem(orderItem);
        // Persist explicit để tránh TransientObjectException khi merge order managed.
        entityManager.persist(orderItem);

        orderTotalCalculator.recalculate(order);
        entityManager.flush();

        OrderResponseDTO dto = orderMapper.toDto(order);
        eventPublisher.broadcast("order.itemAdded", dto);
        return dto;
    }
}
