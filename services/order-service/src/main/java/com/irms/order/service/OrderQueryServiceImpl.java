package com.irms.order.service;

import com.irms.order.domain.Order;
import com.irms.order.exception.OrderNotFoundException;
import com.irms.order.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@SuppressWarnings("null")
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderSpecificationFactory orderSpecificationFactory;

    public OrderQueryServiceImpl(OrderRepository orderRepository,
                                 OrderSpecificationFactory orderSpecificationFactory) {
        this.orderRepository = orderRepository;
        this.orderSpecificationFactory = orderSpecificationFactory;
    }

    @Override
    public Order getOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
    }

    @Override
    public Page<Order> getOrders(OrderSearchCriteria criteria, Pageable pageable) {
        return orderRepository.findAll(orderSpecificationFactory.from(criteria), pageable);
    }
}
