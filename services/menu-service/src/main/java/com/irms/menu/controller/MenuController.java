package com.irms.menu.controller;

import com.irms.menu.domain.MenuItem;
import com.irms.menu.dto.MenuItemRequest;
import com.irms.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/items")
    public ResponseEntity<List<MenuItem>> getAllItems() {
        return ResponseEntity.ok(menuService.getAllMenuItems());
    }

    @PostMapping("/items")
    public ResponseEntity<MenuItem> createItem(@RequestBody MenuItemRequest request) {
        return new ResponseEntity<>(menuService.createMenuItem(request), HttpStatus.CREATED);
    }
}
