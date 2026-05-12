package com.irms.order.service;

import com.irms.order.domain.Order;

import java.util.List;

public interface KitchenOrderQueryService {
    List<Order> getKitchenOrders();
}
