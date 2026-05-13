package com.irms.menu.service;

import com.irms.menu.domain.Category;

import java.util.UUID;

public interface CategoryCommandService {
    Category createCategory(Category category);
    Category updateCategory(UUID id, Category request);
    void deleteCategory(UUID id);
}
