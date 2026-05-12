package com.irms.order.service;

import com.irms.order.domain.Order;
import com.irms.order.domain.OrderStatus;

import java.util.UUID;

public interface OrderCommandService {
    Order createOrder(OrderCreateInput input);
    Order updateOrderStatus(UUID id, OrderStatus newStatus);
    void deleteOrder(UUID id);
}
