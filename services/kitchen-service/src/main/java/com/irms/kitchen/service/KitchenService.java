package com.irms.kitchen.service;

import com.irms.kitchen.domain.KitchenTicket;
import com.irms.kitchen.domain.StationType;
import com.irms.kitchen.domain.TicketItemStatus;
import com.irms.kitchen.dto.CreateTicketRequest;

import java.util.List;
import java.util.UUID;

public interface KitchenService {
    KitchenTicket createTicket(CreateTicketRequest request);
    List<KitchenTicket> getActiveTickets();
    KitchenTicket getTicketById(UUID ticketId);
    void updateItemStatus(UUID itemId, TicketItemStatus newStatus);
    /** Internal sync từ order-service. Trả về số kitchen item đã update. */
    int syncStatusByMenuItem(UUID orderId, UUID menuItemId, TicketItemStatus newStatus);
    void updateTicketStatus(UUID ticketId, com.irms.kitchen.domain.TicketStatus newStatus);
    List<com.irms.kitchen.domain.KitchenTicketItem> getItemsByStation(StationType station);
}
