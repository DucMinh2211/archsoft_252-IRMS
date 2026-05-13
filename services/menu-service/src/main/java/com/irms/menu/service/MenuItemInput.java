package com.irms.menu.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record MenuItemInput(
        String name,
        String description,
        BigDecimal price,
        UUID categoryId,
        Integer preparationTime,
        String imageUrl,
        List<String> allergens,
        Boolean isAvailable
) {
}
