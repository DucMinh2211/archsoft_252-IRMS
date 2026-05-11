package com.irms.order.service;

import com.irms.order.domain.Order;
import com.irms.order.domain.OrderItemStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderTotalCalculator {

    public BigDecimal calculate(Order order) {
        return order.getItems().stream()
                .filter(item -> item.getStatus() != OrderItemStatus.CANCELLED)
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void recalculate(Order order) {
        order.setTotalAmount(calculate(order));
    }
}
