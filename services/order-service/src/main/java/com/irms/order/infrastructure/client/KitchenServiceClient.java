package com.irms.order.infrastructure.client;

import com.irms.order.dto.KitchenTicketRequestDTO;
import com.irms.order.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class KitchenServiceClient implements KitchenTicketClient {

    private final RestTemplate restTemplate;
    private final String kitchenServiceUrl;

    public KitchenServiceClient(RestTemplate restTemplate,
                                @Value("${clients.kitchen-service.base-url}") String kitchenServiceUrl) {
        this.restTemplate = restTemplate;
        this.kitchenServiceUrl = kitchenServiceUrl;
    }

    @Override
    public void createTicket(KitchenTicketRequestDTO request) {
        try {
            String url = kitchenServiceUrl + "/api/v1/kitchen/tickets";
            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ServiceUnavailableException("Failed to create ticket in Kitchen Service. Status: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("Kitchen service is currently unavailable: " + e.getMessage());
        }
    }
}
