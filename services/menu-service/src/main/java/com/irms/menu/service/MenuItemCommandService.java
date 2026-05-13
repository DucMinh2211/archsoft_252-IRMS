package com.irms.menu.service;

import com.irms.menu.domain.MenuItem;

import java.util.UUID;

public interface MenuItemCommandService {
    MenuItem createMenuItem(MenuItemInput input);
    MenuItem updateMenuItem(UUID id, MenuItemInput input);
    void deleteMenuItem(UUID id);
}
