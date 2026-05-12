package com.irms.order.service;

import com.irms.order.domain.Order;
import com.irms.order.domain.OrderItem;
import com.irms.order.domain.OrderItemStatus;
import com.irms.order.domain.OrderStatus;
import com.irms.order.exception.BusinessValidationException;
import com.irms.order.exception.OrderAlreadyCancelledException;
import com.irms.order.exception.OrderNotFoundException;
import com.irms.order.repository.OrderRepository;
import com.irms.order.validator.OrderStateValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@SuppressWarnings("null")
public class OrderCommandServiceImpl implements OrderCommandService {

    private final OrderRepository orderRepository;
    private final OrderItemFactory orderItemFactory;
    private final OrderTotalCalculator orderTotalCalculator;
    private final KitchenTicketDispatcher kitchenTicketDispatcher;
    private final OrderStateValidator stateValidator;
    private final OrderChangeNotifier notifier;
    private final OrderTableValidator orderTableValidator;

    public OrderCommandServiceImpl(OrderRepository orderRepository,
                                   OrderItemFactory orderItemFactory,
                                   OrderTotalCalculator orderTotalCalculator,
                                   KitchenTicketDispatcher kitchenTicketDispatcher,
                                   OrderStateValidator stateValidator,
                                   OrderChangeNotifier notifier,
                                   OrderTableValidator orderTableValidator) {
        this.orderRepository = orderRepository;
        this.orderItemFactory = orderItemFactory;
        this.orderTotalCalculator = orderTotalCalculator;
        this.kitchenTicketDispatcher = kitchenTicketDispatcher;
        this.stateValidator = stateValidator;
        this.notifier = notifier;
        this.orderTableValidator = orderTableValidator;
    }

    @Override
    @Transactional
    public Order createOrder(OrderCreateInput input) {
        orderTableValidator.validateFor(input);

        Order order = new Order();
        order.setTableId(input.tableId());
        order.setWaiterId(input.waiterId());
        order.setType(input.type());
        order.setSpecialNote(input.specialNote());
        order.setStatus(OrderStatus.DRAFT);

        if (input.items() != null && !input.items().isEmpty()) {
            for (OrderItemInput itemInput : input.items()) {
                OrderItem orderItem = orderItemFactory.create(itemInput);
                order.addItem(orderItem);
            }
        }

        orderTotalCalculator.recalculate(order);

        Order savedOrder = orderRepository.save(order);
        notifier.orderCreated(savedOrder);
        return savedOrder;
    }

    @Override
    @Transactional
    public Order updateOrderStatus(UUID id, OrderStatus newStatus) {
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

        Order savedOrder = orderRepository.save(order);
        notifier.orderStatusChanged(savedOrder);
        return savedOrder;
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
        notifier.orderDeleted(id);
    }
}
