package com.irms.order.service;

import com.irms.order.domain.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderSearchCriteria(
        OrderStatus status,
        UUID waiterId,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
