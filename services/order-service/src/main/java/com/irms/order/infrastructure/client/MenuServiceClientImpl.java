package com.irms.order.infrastructure.client;

import com.irms.order.dto.MenuItemDTO;
import com.irms.order.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class MenuServiceClientImpl implements MenuServiceClient {

    private final RestTemplate restTemplate;
    private final String menuServiceUrl;

    public MenuServiceClientImpl(RestTemplate restTemplate, 
                                 @Value("${clients.menu-service.base-url}") String menuServiceUrl) {
        this.restTemplate = restTemplate;
        this.menuServiceUrl = menuServiceUrl;
    }

    @Override
    public MenuItemDTO getMenuItem(UUID menuItemId) {
        try {
            String url = menuServiceUrl + "/api/menu/items/" + menuItemId;
            return restTemplate.getForObject(url, MenuItemDTO.class);
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("Menu service is currently unavailable", e);
        }
    }
}
