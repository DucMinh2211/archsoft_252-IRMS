package com.irms.table.dto;

import com.irms.table.domain.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        String customerName,
        String customerPhone,
        Integer partySize,
        LocalDateTime reservationTime,
        ReservationStatus status,
        String notes,
        TableResponse table,
        Integer expectedDurationMinutes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
