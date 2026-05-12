package com.irms.order.service;

import com.irms.order.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderQueryService {
    Order getOrder(UUID id);
    Page<Order> getOrders(OrderSearchCriteria criteria, Pageable pageable);
}
