package com.irms.order.service;

import com.irms.order.domain.OrderItem;
import com.irms.order.domain.OrderItemStatus;
import com.irms.order.dto.MenuItemDTO;
import com.irms.order.dto.OrderItemRequestDTO;
import com.irms.order.infrastructure.client.MenuServiceClient;
import org.springframework.stereotype.Component;

@Component
public class OrderItemFactory {

    private final MenuServiceClient menuServiceClient;

    public OrderItemFactory(MenuServiceClient menuServiceClient) {
        this.menuServiceClient = menuServiceClient;
    }

    public OrderItem create(OrderItemRequestDTO itemDTO) {
        MenuItemDTO menuItem = menuServiceClient.getMenuItem(itemDTO.getMenuItemId());

        OrderItem orderItem = new OrderItem();
        orderItem.setMenuItemId(menuItem.getId());
        orderItem.setMenuItemName(menuItem.getName());
        orderItem.setQuantity(itemDTO.getQuantity());
        orderItem.setPrice(menuItem.getPrice());
        orderItem.setNote(itemDTO.getNote());
        orderItem.setStatus(OrderItemStatus.PENDING);

        return orderItem;
    }
}
