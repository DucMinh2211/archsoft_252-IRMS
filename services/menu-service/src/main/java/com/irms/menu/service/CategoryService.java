package com.irms.menu.service;

import com.irms.menu.domain.Category;
import com.irms.menu.exception.DuplicateResourceException;
import com.irms.menu.exception.ResourceNotFoundException;
import com.irms.menu.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CategoryService implements CategoryQueryService, CategoryCommandService {

    private final CategoryRepository categoryRepository;
    private final CategoryDeletionPolicy categoryDeletionPolicy;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new DuplicateResourceException("Category already exists: " + category.getName());
        }
        return categoryRepository.save(category);
    }

    @Override
    public Category getCategoryById(java.util.UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Override
    @Transactional
    public Category updateCategory(java.util.UUID id, Category request) {
        Category category = getCategoryById(id);
        
        if (!category.getName().equals(request.getName()) && categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category name already exists: " + request.getName());
        }
        
        category.setName(request.getName());
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(java.util.UUID id) {
        Category category = getCategoryById(id);
        categoryDeletionPolicy.validateCanDelete(id);
        
        categoryRepository.delete(category);
    }
}
