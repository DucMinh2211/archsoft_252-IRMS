package com.irms.menu.service;

import com.irms.menu.exception.BusinessRuleViolationException;
import com.irms.menu.repository.MenuItemRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CategoryDeletionPolicy {

    private final MenuItemRepository menuItemRepository;

    public CategoryDeletionPolicy(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    public void validateCanDelete(UUID categoryId) {
        if (!menuItemRepository.findByCategoryId(categoryId).isEmpty()) {
            throw new BusinessRuleViolationException("Cannot delete category. It contains active menu items.");
        }
    }
}
