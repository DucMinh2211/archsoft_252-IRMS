package com.irms.table.dto;

import com.irms.table.domain.TableStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TableResponse(
        UUID id,
        String tableNumber,
        Integer capacity,
        TableStatus status,
        String location,
        UUID currentOrderId,
        LocalDateTime seatedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
