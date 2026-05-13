package com.irms.menu.service;

import com.irms.menu.domain.MenuItem;
import com.irms.menu.exception.BusinessRuleViolationException;
import com.irms.menu.repository.MenuItemRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CategoryDeletionPolicyTest {

    @Test
    void validateCanDelete_ShouldAllowEmptyCategory() {
        UUID categoryId = UUID.randomUUID();
        MenuItemRepository repository = mock(MenuItemRepository.class);
        when(repository.findByCategoryId(categoryId)).thenReturn(List.of());

        CategoryDeletionPolicy policy = new CategoryDeletionPolicy(repository);

        assertDoesNotThrow(() -> policy.validateCanDelete(categoryId));
    }

    @Test
    void validateCanDelete_ShouldRejectCategoryWithItems() {
        UUID categoryId = UUID.randomUUID();
        MenuItemRepository repository = mock(MenuItemRepository.class);
        when(repository.findByCategoryId(categoryId)).thenReturn(List.of(new MenuItem()));

        CategoryDeletionPolicy policy = new CategoryDeletionPolicy(repository);

        assertThrows(BusinessRuleViolationException.class, () -> policy.validateCanDelete(categoryId));
    }
}
