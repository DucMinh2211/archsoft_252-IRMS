package com.irms.payment.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class OrderServiceClient {

    private final RestTemplate restTemplate;
    private final String orderServiceUrl;

    public OrderServiceClient(RestTemplate restTemplate,
                              @Value("${clients.order-service.base-url}") String orderServiceUrl) {
        this.restTemplate = restTemplate;
        this.orderServiceUrl = orderServiceUrl;
    }

    public void updateOrderStatusToCompleted(UUID orderId) {
        try {
            String url = orderServiceUrl + "/api/v1/orders/" + orderId + "/status?status=COMPLETED";
            restTemplate.put(url, null);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to notify order-service of completion: " + e.getMessage());
        }
    }
}
