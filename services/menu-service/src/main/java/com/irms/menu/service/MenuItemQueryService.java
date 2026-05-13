package com.irms.menu.service;

import com.irms.menu.domain.MenuItem;

import java.util.List;
import java.util.UUID;

public interface MenuItemQueryService {
    List<MenuItem> getAllMenuItems();
    MenuItem getMenuItemById(UUID id);
    List<MenuItem> getItemsByCategory(UUID categoryId);
}
