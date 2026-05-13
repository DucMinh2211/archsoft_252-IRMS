package com.irms.order.service;

import com.irms.order.domain.Order;
import com.irms.order.domain.OrderStatus;
import com.irms.order.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KitchenOrderQueryServiceImpl implements KitchenOrderQueryService {

    private final OrderRepository orderRepository;

    public KitchenOrderQueryServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public List<Order> getKitchenOrders() {
        return orderRepository.findByStatusIn(List.of(OrderStatus.PENDING, OrderStatus.COOKING));
    }
}
