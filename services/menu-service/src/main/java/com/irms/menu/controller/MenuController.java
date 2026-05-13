package com.irms.menu.controller;

import com.irms.menu.dto.MenuItemRequest;
import com.irms.menu.dto.MenuItemResponse;
import com.irms.menu.mapper.MenuResponseMapper;
import com.irms.menu.service.MenuItemCommandService;
import com.irms.menu.service.MenuItemQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuItemQueryService menuItemQueryService;
    private final MenuItemCommandService menuItemCommandService;
    private final MenuResponseMapper menuResponseMapper;

    @GetMapping("/items")
    public ResponseEntity<List<MenuItemResponse>> getAllItems() {
        return ResponseEntity.ok(menuResponseMapper.toMenuItemResponses(menuItemQueryService.getAllMenuItems()));
    }

    @PostMapping("/items")
    public ResponseEntity<MenuItemResponse> createItem(@RequestBody MenuItemRequest request) {
        return new ResponseEntity<>(
                menuResponseMapper.toMenuItemResponse(menuItemCommandService.createMenuItem(menuResponseMapper.toMenuItemInput(request))),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<MenuItemResponse> getItemById(@PathVariable UUID id) {
        return ResponseEntity.ok(menuResponseMapper.toMenuItemResponse(menuItemQueryService.getMenuItemById(id)));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<MenuItemResponse> updateItem(@PathVariable UUID id, @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(
                menuResponseMapper.toMenuItemResponse(menuItemCommandService.updateMenuItem(id, menuResponseMapper.toMenuItemInput(request)))
        );
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        menuItemCommandService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories/{categoryId}/items")
    public ResponseEntity<List<MenuItemResponse>> getItemsByCategory(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(menuResponseMapper.toMenuItemResponses(menuItemQueryService.getItemsByCategory(categoryId)));
    }
}
