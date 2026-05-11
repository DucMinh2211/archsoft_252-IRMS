package com.irms.kitchen.service;

import com.irms.kitchen.domain.KitchenTicket;
import com.irms.kitchen.domain.KitchenTicketItem;
import com.irms.kitchen.domain.StationType;
import com.irms.kitchen.domain.TicketItemStatus;
import com.irms.kitchen.domain.TicketStatus;

import java.util.List;
import java.util.UUID;

public interface KdsService {
    List<KitchenTicket> getActiveTickets();
    void updateItemStatus(UUID itemId, TicketItemStatus newStatus);
    void updateTicketStatus(UUID ticketId, TicketStatus newStatus);
    List<KitchenTicketItem> getItemsByStation(StationType station);
}
