package com.irms.order.infrastructure.client;

import com.irms.order.dto.KitchenTicketRequestDTO;

public interface KitchenTicketClient {
    void createTicket(KitchenTicketRequestDTO request);
}
