package com.irms.menu.service;

import com.irms.menu.domain.Category;
import com.irms.menu.domain.MenuItem;
import com.irms.menu.dto.MenuItemRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class MenuItemFactory {

    public MenuItem create(MenuItemRequest request, Category category) {
        return MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(category)
                .preparationTime(request.getPreparationTime())
                .imageUrl(request.getImageUrl())
                .allergens(request.getAllergens() != null ? request.getAllergens() : new ArrayList<>())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .build();
    }
}
