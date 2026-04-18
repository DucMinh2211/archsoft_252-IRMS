package com.irms.menu.service;

import com.irms.menu.domain.Category;
import com.irms.menu.domain.MenuItem;
import com.irms.menu.dto.MenuItemRequest;
import com.irms.menu.repository.CategoryRepository;
import com.irms.menu.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;

    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public MenuItem createMenuItem(MenuItemRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));

        MenuItem menuItem = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(category)
                .preparationTime(request.getPreparationTime())
                .imageUrl(request.getImageUrl())
                .isAvailable(true)
                .build();

        return menuItemRepository.save(menuItem);
    }
}
