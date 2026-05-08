package com.irms.order.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Đẩy thay đổi từ POS (waiter cancel món / mark served) sang kitchen-service để chef thấy.
 * Dùng endpoint internal của kitchen-service để TRÁNH LOOP (kitchen sẽ không gọi lại order).
 */
@Slf4j
@Component
public class KitchenSyncClient {

    private final RestTemplate restTemplate;
    private final String kitchenServiceUrl;

    public KitchenSyncClient(RestTemplate restTemplate,
                             @Value("${clients.kitchen-service.base-url}") String kitchenServiceUrl) {
        this.restTemplate = restTemplate;
        this.kitchenServiceUrl = kitchenServiceUrl;
    }

    public void syncItemStatus(UUID orderId, UUID menuItemId, String orderItemStatus) {
        try {
            String kitchenStatus = mapToKitchenStatus(orderItemStatus);
            if (kitchenStatus == null) return; // Không map được → bỏ qua (vd: SERVED không có ở kitchen)
            String url = kitchenServiceUrl + "/api/v1/internal/kitchen/orders/" + orderId
                    + "/menu/" + menuItemId + "/status?status=" + kitchenStatus;
            restTemplate.put(url, null);
        } catch (RestClientException e) {
            log.warn("Sync to kitchen failed (orderId={}, menuItemId={}, status={}): {}",
                    orderId, menuItemId, orderItemStatus, e.getMessage());
        }
    }

    private String mapToKitchenStatus(String orderItemStatus) {
        return switch (orderItemStatus) {
            case "PENDING"        -> "PENDING";
            case "COOKING"        -> "COOKING";
            case "READY_TO_SERVE" -> "READY";
            case "CANCELLED"      -> "CANCELLED";
            // SERVED không map: kitchen ticket items không có "SERVED" — chef không quan tâm sau READY
            default -> null;
        };
    }
}
