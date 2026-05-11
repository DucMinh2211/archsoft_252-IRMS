package com.irms.menu.service;

import com.irms.menu.domain.Category;
import com.irms.menu.domain.MenuItem;
import com.irms.menu.dto.MenuItemRequest;
import org.springframework.stereotype.Component;

@Component
public class MenuItemUpdater {

    public void update(MenuItem menuItem, MenuItemRequest request, Category category) {
        if (category != null) {
            menuItem.setCategory(category);
        }
        if (request.getName() != null) menuItem.setName(request.getName());
        if (request.getDescription() != null) menuItem.setDescription(request.getDescription());
        if (request.getPrice() != null) menuItem.setPrice(request.getPrice());
        if (request.getPreparationTime() != null) menuItem.setPreparationTime(request.getPreparationTime());
        if (request.getImageUrl() != null) menuItem.setImageUrl(request.getImageUrl());
        if (request.getIsAvailable() != null) menuItem.setIsAvailable(request.getIsAvailable());
        if (request.getAllergens() != null) {
            menuItem.getAllergens().clear();
            menuItem.getAllergens().addAll(request.getAllergens());
        }
    }
}
