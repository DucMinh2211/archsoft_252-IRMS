package com.irms.kitchen.controller;

import com.irms.kitchen.domain.TicketItemStatus;
import com.irms.kitchen.service.KitchenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Endpoint nội bộ — chỉ dùng để order-service đẩy thay đổi xuống kitchen mà không gây loop.
 * Không expose qua public API gateway.
 */
@RestController
@RequestMapping("/api/v1/internal/kitchen")
@RequiredArgsConstructor
public class InternalSyncController {

    private final KitchenService kitchenService;

    @PutMapping("/orders/{orderId}/menu/{menuItemId}/status")
    public ResponseEntity<Map<String, Integer>> syncStatusByMenuItem(
            @PathVariable UUID orderId,
            @PathVariable UUID menuItemId,
            @RequestParam TicketItemStatus status) {
        int updated = kitchenService.syncStatusByMenuItem(orderId, menuItemId, status);
        return ResponseEntity.ok(Map.of("updated", updated));
    }
}
