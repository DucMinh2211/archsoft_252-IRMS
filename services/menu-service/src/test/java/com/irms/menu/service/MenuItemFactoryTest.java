package com.irms.menu.service;

import com.irms.menu.domain.Category;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuItemFactoryTest {

    private final MenuItemFactory factory = new MenuItemFactory();

    @Test
    void create_ShouldPreserveExistingDefaults() {
        Category category = new Category();
        MenuItemInput input = new MenuItemInput(
                "Burger",
                "House burger",
                new BigDecimal("12.50"),
                null,
                15,
                null,
                null,
                null
        );

        var item = factory.create(input, category);

        assertEquals("Burger", item.getName());
        assertEquals("House burger", item.getDescription());
        assertEquals(new BigDecimal("12.50"), item.getPrice());
        assertEquals(15, item.getPreparationTime());
        assertSame(category, item.getCategory());
        assertTrue(item.getIsAvailable());
        assertTrue(item.getAllergens().isEmpty());
    }
}
