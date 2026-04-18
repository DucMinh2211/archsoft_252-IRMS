package com.irms.menu.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class MenuItemRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private UUID categoryId;
    private Integer preparationTime;
    private String imageUrl;
}
