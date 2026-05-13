package com.irms.menu.service;

import com.irms.menu.domain.Category;
import com.irms.menu.domain.MenuItem;
import org.springframework.stereotype.Component;

@Component
public class MenuItemUpdater {

    public void update(MenuItem menuItem, MenuItemInput input, Category category) {
        if (category != null) {
            menuItem.setCategory(category);
        }
        if (input.name() != null) menuItem.setName(input.name());
        if (input.description() != null) menuItem.setDescription(input.description());
        if (input.price() != null) menuItem.setPrice(input.price());
        if (input.preparationTime() != null) menuItem.setPreparationTime(input.preparationTime());
        if (input.imageUrl() != null) menuItem.setImageUrl(input.imageUrl());
        if (input.isAvailable() != null) menuItem.setIsAvailable(input.isAvailable());
        if (input.allergens() != null) {
            menuItem.getAllergens().clear();
            menuItem.getAllergens().addAll(input.allergens());
        }
    }
}
