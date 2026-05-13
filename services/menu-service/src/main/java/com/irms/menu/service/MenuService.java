package com.irms.menu.service;

import com.irms.menu.domain.Category;
import com.irms.menu.domain.MenuItem;
import com.irms.menu.exception.ResourceNotFoundException;
import com.irms.menu.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class MenuService implements MenuItemQueryService, MenuItemCommandService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryQueryService categoryQueryService;
    private final MenuItemFactory menuItemFactory;
    private final MenuItemUpdater menuItemUpdater;

    @Override
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    @Override
    @Transactional
    public MenuItem createMenuItem(MenuItemInput input) {
        Category category = categoryQueryService.getCategoryById(input.categoryId());

        MenuItem menuItem = menuItemFactory.create(input, category);
        return menuItemRepository.save(menuItem);
    }

    @Override
    public MenuItem getMenuItemById(UUID id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));
    }

    @Override
    public List<MenuItem> getItemsByCategory(UUID categoryId) {
        return menuItemRepository.findByCategoryId(categoryId);
    }

    @Override
    @Transactional
    public MenuItem updateMenuItem(UUID id, MenuItemInput input) {
        MenuItem menuItem = getMenuItemById(id);
        Category category = null;
        
        if (input.categoryId() != null && !input.categoryId().equals(menuItem.getCategory().getId())) {
            category = categoryQueryService.getCategoryById(input.categoryId());
        }

        menuItemUpdater.update(menuItem, input, category);
        
        return menuItemRepository.save(menuItem);
    }

    @Override
    @Transactional
    public void deleteMenuItem(UUID id) {
        MenuItem menuItem = getMenuItemById(id);
        // Soft delete
        menuItem.setIsAvailable(false);
        menuItemRepository.save(menuItem);
    }
}
