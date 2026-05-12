package com.irms.order.service;

import com.irms.order.domain.Order;
import com.irms.order.domain.OrderItem;
import com.irms.order.domain.OrderItemStatus;
import com.irms.order.mapper.OrderMapper;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class OrderChangeNotifier {

    private final OrderEventPublisher eventPublisher;
    private final OrderMapper orderMapper;

    public OrderChangeNotifier(OrderEventPublisher eventPublisher, OrderMapper orderMapper) {
        this.eventPublisher = eventPublisher;
        this.orderMapper = orderMapper;
    }

    public void orderCreated(Order order) {
        eventPublisher.broadcast("order.created", orderMapper.toDto(order));
    }

    public void orderStatusChanged(Order order) {
        eventPublisher.broadcast("order.status", orderMapper.toDto(order));
    }

    public void orderDeleted(UUID id) {
        eventPublisher.broadcast("order.deleted", id);
    }

    public void itemAdded(Order order) {
        eventPublisher.broadcast("order.itemAdded", orderMapper.toDto(order));
    }

    public void itemStatusChanged(OrderItem item) {
        eventPublisher.broadcast("order.itemStatus", orderMapper.toDto(item));
    }

    public void itemStatusSynced(UUID orderId, UUID menuItemId, OrderItemStatus status, int updated) {
        eventPublisher.broadcast("order.itemStatus.sync",
                Map.of("orderId", orderId, "menuItemId", menuItemId, "status", status.name(), "updated", updated));
    }
}
