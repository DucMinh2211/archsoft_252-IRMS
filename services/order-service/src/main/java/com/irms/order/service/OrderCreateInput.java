package com.irms.order.service;

import com.irms.order.domain.OrderType;

import java.util.List;
import java.util.UUID;

public record OrderCreateInput(
        UUID tableId,
        UUID waiterId,
        OrderType type,
        String specialNote,
        List<OrderItemInput> items
) {
}
