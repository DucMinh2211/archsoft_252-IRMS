package com.irms.order.infrastructure.client;

import org.springframework.stereotype.Component;

@Component
public class KitchenItemStatusMapper {

    public String toKitchenStatus(String orderItemStatus) {
        return switch (orderItemStatus) {
            case "PENDING" -> "PENDING";
            case "COOKING" -> "COOKING";
            case "READY_TO_SERVE" -> "READY";
            case "SERVED" -> "SERVED";
            case "CANCELLED" -> "CANCELLED";
            default -> null;
        };
    }
}
