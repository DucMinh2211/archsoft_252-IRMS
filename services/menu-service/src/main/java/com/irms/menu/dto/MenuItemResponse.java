package com.irms.menu.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MenuItemResponse(
        UUID id,
        CategorySummaryResponse category,
        String name,
        String description,
        BigDecimal price,
        Boolean isAvailable,
        Integer preparationTime,
        String imageUrl,
        List<String> allergens,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
