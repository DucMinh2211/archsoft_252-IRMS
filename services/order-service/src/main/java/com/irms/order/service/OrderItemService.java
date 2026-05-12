package com.irms.order.service;

import com.irms.order.domain.OrderItemStatus;
import com.irms.order.domain.Order;
import com.irms.order.domain.OrderItem;

import java.util.UUID;

public interface OrderItemService {
    Order addOrderItem(UUID orderId, OrderItemInput input);
    OrderItem updateOrderItem(UUID itemId, OrderItemInput input);
    OrderItem updateOrderItemStatus(UUID itemId, OrderItemStatus newStatus);
    void deleteOrderItem(UUID itemId);
}
