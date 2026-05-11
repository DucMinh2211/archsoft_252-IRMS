package com.irms.menu.controller;

import com.irms.menu.dto.CategoryRequest;
import com.irms.menu.dto.CategoryResponse;
import com.irms.menu.mapper.MenuResponseMapper;
import com.irms.menu.service.CategoryCommandService;
import com.irms.menu.service.CategoryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/menu/categories")
@RequiredArgsConstructor
public class MenuCategoryController {

    private final CategoryQueryService categoryQueryService;
    private final CategoryCommandService categoryCommandService;
    private final MenuResponseMapper menuResponseMapper;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(menuResponseMapper.toCategoryResponses(categoryQueryService.getAllCategories()));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        return new ResponseEntity<>(menuResponseMapper.toCategoryResponse(categoryCommandService.createCategory(menuResponseMapper.toCategory(request))), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(menuResponseMapper.toCategoryResponse(categoryQueryService.getCategoryById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable UUID id, @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(menuResponseMapper.toCategoryResponse(categoryCommandService.updateCategory(id, menuResponseMapper.toCategory(request))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryCommandService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
