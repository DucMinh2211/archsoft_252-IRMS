package com.irms.kitchen.service;

import com.irms.kitchen.domain.TicketItemStatus;
import org.springframework.stereotype.Component;

@Component
public class OrderItemStatusMapper {

    public String toOrderItemStatus(TicketItemStatus status) {
        return switch (status) {
            case PENDING -> "PENDING";
            case COOKING -> "COOKING";
            case READY -> "READY_TO_SERVE";
            case SERVED -> "SERVED";
            case CANCELLED -> "CANCELLED";
        };
    }
}
