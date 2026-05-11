package com.irms.order.service;

import com.irms.order.domain.Order;
import com.irms.order.domain.OrderItem;
import com.irms.order.domain.OrderItemStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderTotalCalculatorTest {

    private final OrderTotalCalculator calculator = new OrderTotalCalculator();

    @Test
    void calculate_ShouldExcludeCancelledItems() {
        Order order = new Order();
        order.addItem(item("10.00", 2, OrderItemStatus.PENDING));
        order.addItem(item("7.50", 1, OrderItemStatus.CANCELLED));
        order.addItem(item("3.25", 4, OrderItemStatus.SERVED));

        assertEquals(new BigDecimal("33.00"), calculator.calculate(order));
    }

    @Test
    void recalculate_ShouldUpdateOrderTotalAmount() {
        Order order = new Order();
        order.addItem(item("12.00", 1, OrderItemStatus.PENDING));

        calculator.recalculate(order);

        assertEquals(new BigDecimal("12.00"), order.getTotalAmount());
    }

    private OrderItem item(String price, int quantity, OrderItemStatus status) {
        OrderItem item = new OrderItem();
        item.setPrice(new BigDecimal(price));
        item.setQuantity(quantity);
        item.setStatus(status);
        return item;
    }
}
