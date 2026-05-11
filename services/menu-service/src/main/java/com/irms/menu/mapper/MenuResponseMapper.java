package com.irms.menu.mapper;

import com.irms.menu.domain.Category;
import com.irms.menu.domain.MenuItem;
import com.irms.menu.dto.CategoryRequest;
import com.irms.menu.dto.CategoryResponse;
import com.irms.menu.dto.CategorySummaryResponse;
import com.irms.menu.dto.MenuItemResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MenuResponseMapper {

    public MenuItemResponse toMenuItemResponse(MenuItem item) {
        if (item == null) {
            return null;
        }

        return new MenuItemResponse(
                item.getId(),
                toCategorySummaryResponse(item.getCategory()),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getIsAvailable(),
                item.getPreparationTime(),
                item.getImageUrl(),
                item.getAllergens(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    public List<MenuItemResponse> toMenuItemResponses(List<MenuItem> items) {
        return items.stream().map(this::toMenuItemResponse).toList();
    }

    public CategoryResponse toCategoryResponse(Category category) {
        if (category == null) {
            return null;
        }

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDisplayOrder(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    public List<CategoryResponse> toCategoryResponses(List<Category> categories) {
        return categories.stream().map(this::toCategoryResponse).toList();
    }

    public Category toCategory(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        return category;
    }

    private CategorySummaryResponse toCategorySummaryResponse(Category category) {
        if (category == null) {
            return null;
        }

        return new CategorySummaryResponse(
                category.getId(),
                category.getName(),
                category.getDisplayOrder()
        );
    }
}
