package com.irms.menu.service;

import com.irms.menu.domain.Category;
import com.irms.menu.domain.MenuItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class MenuItemFactory {

    public MenuItem create(MenuItemInput input, Category category) {
        return MenuItem.builder()
                .name(input.name())
                .description(input.description())
                .price(input.price())
                .category(category)
                .preparationTime(input.preparationTime())
                .imageUrl(input.imageUrl())
                .allergens(input.allergens() != null ? input.allergens() : new ArrayList<>())
                .isAvailable(input.isAvailable() != null ? input.isAvailable() : true)
                .build();
    }
}
