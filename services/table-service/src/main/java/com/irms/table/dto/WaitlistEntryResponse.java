package com.irms.table.dto;

import com.irms.table.domain.WaitlistStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record WaitlistEntryResponse(
        UUID id,
        String customerName,
        String customerPhone,
        Integer partySize,
        WaitlistStatus status,
        Integer estimatedWaitMinutes,
        LocalDateTime notifiedAt,
        Integer queuePosition,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
