package com.irms.menu.service;

import com.irms.menu.domain.Category;
import com.irms.menu.domain.MenuItem;
import com.irms.menu.dto.MenuItemRequest;
import com.irms.menu.exception.ResourceNotFoundException;
import com.irms.menu.repository.CategoryRepository;
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
    private final CategoryRepository categoryRepository;
    private final MenuItemFactory menuItemFactory;
    private final MenuItemUpdater menuItemUpdater;

    @Override
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional
    public MenuItem createMenuItem(MenuItemRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        MenuItem menuItem = menuItemFactory.create(request, category);
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
    public MenuItem updateMenuItem(UUID id, MenuItemRequest request) {
        MenuItem menuItem = getMenuItemById(id);
        Category category = null;
        
        if (request.getCategoryId() != null && !request.getCategoryId().equals(menuItem.getCategory().getId())) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        }

        menuItemUpdater.update(menuItem, request, category);
        
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
