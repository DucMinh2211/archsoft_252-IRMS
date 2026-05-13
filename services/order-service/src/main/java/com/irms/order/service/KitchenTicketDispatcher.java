package com.irms.order.service;

import com.irms.order.domain.Order;
import com.irms.order.domain.OrderItemStatus;
import com.irms.order.dto.KitchenTicketItemRequestDTO;
import com.irms.order.dto.KitchenTicketRequestDTO;
import com.irms.order.infrastructure.client.KitchenTicketClient;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class KitchenTicketDispatcher {

    private final KitchenTicketClient kitchenTicketClient;

    public KitchenTicketDispatcher(KitchenTicketClient kitchenTicketClient) {
        this.kitchenTicketClient = kitchenTicketClient;
    }

    public void dispatch(Order order) {
        KitchenTicketRequestDTO ticketRequest = KitchenTicketRequestDTO.builder()
                .orderId(order.getId())
                .tableId(order.getTableId())
                .items(order.getItems().stream()
                        .filter(item -> item.getStatus() != OrderItemStatus.CANCELLED)
                        .map(item -> KitchenTicketItemRequestDTO.builder()
                                .menuItemId(item.getMenuItemId())
                                .menuItemName(item.getMenuItemName())
                                .quantity(item.getQuantity())
                                .notes(item.getNote())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        kitchenTicketClient.createTicket(ticketRequest);
    }
}
