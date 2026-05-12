package com.irms.order.service;

import java.util.UUID;

public record OrderItemInput(
        UUID menuItemId,
        Integer quantity,
        String note
) {
}
