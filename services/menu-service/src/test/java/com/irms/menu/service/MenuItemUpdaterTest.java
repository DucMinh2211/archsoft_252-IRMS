package com.irms.menu.service;

import com.irms.menu.domain.Category;
import com.irms.menu.domain.MenuItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class MenuItemUpdaterTest {

    private final MenuItemUpdater updater = new MenuItemUpdater();

    @Test
    void update_ShouldOnlyApplyProvidedFields() {
        Category oldCategory = new Category();
        Category newCategory = new Category();
        MenuItem item = MenuItem.builder()
                .name("Old")
                .description("Old desc")
                .price(new BigDecimal("8.00"))
                .category(oldCategory)
                .allergens(new ArrayList<>(List.of("nuts")))
                .isAvailable(true)
                .build();

        MenuItemInput input = new MenuItemInput(
                "New",
                null,
                null,
                null,
                null,
                null,
                List.of("milk"),
                null
        );

        updater.update(item, input, newCategory);

        assertEquals("New", item.getName());
        assertEquals("Old desc", item.getDescription());
        assertEquals(new BigDecimal("8.00"), item.getPrice());
        assertSame(newCategory, item.getCategory());
        assertEquals(List.of("milk"), item.getAllergens());
    }
}
