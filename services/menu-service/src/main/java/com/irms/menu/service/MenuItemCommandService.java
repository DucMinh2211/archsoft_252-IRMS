package com.irms.menu.service;

import com.irms.menu.domain.MenuItem;
import com.irms.menu.dto.MenuItemRequest;

import java.util.UUID;

public interface MenuItemCommandService {
    MenuItem createMenuItem(MenuItemRequest request);
    MenuItem updateMenuItem(UUID id, MenuItemRequest request);
    void deleteMenuItem(UUID id);
}
