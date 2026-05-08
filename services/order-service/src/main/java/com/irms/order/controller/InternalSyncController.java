package com.irms.order.controller;

import com.irms.order.domain.OrderItemStatus;
import com.irms.order.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Endpoint nội bộ giữa các microservice — KHÔNG nên expose qua API gateway public.
 * Dùng để kitchen-service đẩy thay đổi status xuống order_items mà không gây loop sync.
 */
@RestController
@RequestMapping("/api/v1/internal/orders")
@RequiredArgsConstructor
public class InternalSyncController {

    private final OrderItemService orderItemService;

    @PutMapping("/{orderId}/items/by-menu/{menuItemId}/status")
    public ResponseEntity<Map<String, Integer>> syncStatusByMenuItem(
            @PathVariable UUID orderId,
            @PathVariable UUID menuItemId,
            @RequestParam OrderItemStatus status) {
        int updated = orderItemService.syncStatusByMenuItem(orderId, menuItemId, status);
        return ResponseEntity.ok(Map.of("updated", updated));
    }
}
