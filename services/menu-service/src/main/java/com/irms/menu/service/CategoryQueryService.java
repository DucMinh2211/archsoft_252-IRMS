package com.irms.menu.service;

import com.irms.menu.domain.Category;

import java.util.List;
import java.util.UUID;

public interface CategoryQueryService {
    List<Category> getAllCategories();
    Category getCategoryById(UUID id);
}
