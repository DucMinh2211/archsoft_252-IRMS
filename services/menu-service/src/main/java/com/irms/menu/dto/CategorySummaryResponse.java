package com.irms.menu.dto;

import java.util.UUID;

public record CategorySummaryResponse(
        UUID id,
        String name,
        Integer displayOrder
) {
}
