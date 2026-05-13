package com.irms.order.service;

import com.irms.order.domain.Order;
import com.irms.order.domain.OrderItem;
import com.irms.order.domain.OrderItemStatus;
import com.irms.order.domain.OrderStatus;
import com.irms.order.exception.BusinessValidationException;
import com.irms.order.exception.OrderNotFoundException;
import com.irms.order.infrastructure.client.KitchenItemStatusSyncClient;
import com.irms.order.repository.OrderItemRepository;
import com.irms.order.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@SuppressWarnings("null")
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final KitchenItemStatusSyncClient kitchenSyncClient;
    private final OrderTotalCalculator orderTotalCalculator;
    private final OrderItemStatusTransitionPolicy itemStatusTransitionPolicy;
    private final OrderItemFactory orderItemFactory;
    private final OrderChangeNotifier notifier;

    @PersistenceContext
    private EntityManager entityManager;

    public OrderItemServiceImpl(OrderItemRepository orderItemRepository,
                                OrderRepository orderRepository,
                                KitchenItemStatusSyncClient kitchenSyncClient,
                                OrderTotalCalculator orderTotalCalculator,
                                OrderItemStatusTransitionPolicy itemStatusTransitionPolicy,
                                OrderItemFactory orderItemFactory,
                                OrderChangeNotifier notifier) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.kitchenSyncClient = kitchenSyncClient;
        this.orderTotalCalculator = orderTotalCalculator;
        this.itemStatusTransitionPolicy = itemStatusTransitionPolicy;
        this.orderItemFactory = orderItemFactory;
        this.notifier = notifier;
    }

    @Override
    @Transactional
    public Order addOrderItem(UUID orderId, OrderItemInput input) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessValidationException("Cannot add items to a completed or cancelled order.");
        }

        OrderItem orderItem = orderItemFactory.create(input);
        order.addItem(orderItem);
        // Persist explicit để tránh TransientObjectException khi merge order managed.
        entityManager.persist(orderItem);

        orderTotalCalculator.recalculate(order);
        entityManager.flush();

        notifier.itemAdded(order);
        return order;
    }

    @Override
    @Transactional
    public OrderItem updateOrderItem(UUID itemId, OrderItemInput input) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new OrderNotFoundException("Order Item not found"));
        
        if (item.getStatus() != OrderItemStatus.PENDING) {
            throw new BusinessValidationException("Cannot update item that is already being cooked or served.");
        }

        item.setQuantity(input.quantity());
        item.setNote(input.note());
        
        OrderItem savedItem = orderItemRepository.save(item);
        recalculateOrderTotal(item.getOrder());
        
        return savedItem;
    }

    @Override
    @Transactional
    public OrderItem updateOrderItemStatus(UUID itemId, OrderItemStatus newStatus) {
        return updateOrderItemStatusInternal(itemId, newStatus, true);
    }

    private OrderItem updateOrderItemStatusInternal(UUID itemId, OrderItemStatus newStatus, boolean propagateToKitchen) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new OrderNotFoundException("Order Item not found"));

        OrderItemStatus currentStatus = item.getStatus();
        itemStatusTransitionPolicy.validateTransition(currentStatus, newStatus);

        item.setStatus(newStatus);
        OrderItem savedItem = orderItemRepository.save(item);

        if (newStatus == OrderItemStatus.CANCELLED) {
            recalculateOrderTotal(item.getOrder());
        }

        // POS hủy món / mark served → báo kitchen để chef thấy đồng bộ.
        if (propagateToKitchen && (newStatus == OrderItemStatus.CANCELLED || newStatus == OrderItemStatus.SERVED)) {
            kitchenSyncClient.syncItemStatus(item.getOrder().getId(), item.getMenuItemId(), newStatus.name());
        }

        notifier.itemStatusChanged(savedItem);
        return savedItem;
    }

    @Override
    @Transactional
    public void deleteOrderItem(UUID itemId) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new OrderNotFoundException("Order Item not found"));
        
        if (item.getStatus() != OrderItemStatus.PENDING) {
            throw new BusinessValidationException("Cannot delete item that is already being cooked or served.");
        }
        
        Order order = item.getOrder();
        order.removeItem(item);
        
        orderItemRepository.delete(item);
        recalculateOrderTotal(order);
    }

    private void recalculateOrderTotal(Order order) {
        orderTotalCalculator.recalculate(order);
        orderRepository.save(order);
    }
}
