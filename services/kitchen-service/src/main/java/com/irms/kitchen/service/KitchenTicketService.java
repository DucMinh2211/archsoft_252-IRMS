package com.irms.kitchen.service;

import com.irms.kitchen.domain.KitchenTicket;
import com.irms.kitchen.dto.CreateTicketRequest;

import java.util.UUID;

public interface KitchenTicketService {
    KitchenTicket createTicket(CreateTicketRequest request);
    KitchenTicket getTicketById(UUID ticketId);
}
