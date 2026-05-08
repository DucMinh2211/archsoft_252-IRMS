package com.irms.kitchen.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Khi chef cập nhật trạng thái món ở KDS, đẩy ngược về order-service để POS thấy đồng bộ.
 * order_items và kitchen_ticket_items là 2 bảng ở 2 DB riêng — phải sync thủ công.
 */
@Slf4j
@Component
public class OrderServiceClient {

    private final RestTemplate restTemplate;
    private final String orderServiceUrl;

    public OrderServiceClient(RestTemplate restTemplate,
                              @Value("${clients.order-service.base-url}") String orderServiceUrl) {
        this.restTemplate = restTemplate;
        this.orderServiceUrl = orderServiceUrl;
    }

    /**
     * Đồng bộ status xuống các order_items trong order có cùng menuItemId.
     * Best-effort: log lỗi nhưng không throw — KDS vẫn coi như cập nhật thành công.
     */
    public void syncItemStatus(UUID orderId, UUID menuItemId, String orderItemStatus) {
        try {
            // Dùng endpoint internal để order-service KHÔNG gọi ngược lại kitchen (chống loop).
            String url = orderServiceUrl + "/api/v1/internal/orders/" + orderId
                    + "/items/by-menu/" + menuItemId + "/status?status=" + orderItemStatus;
            restTemplate.put(url, null);
        } catch (RestClientException e) {
            log.warn("Sync order_item status failed (orderId={}, menuItemId={}, status={}): {}",
                    orderId, menuItemId, orderItemStatus, e.getMessage());
        }
    }
}
