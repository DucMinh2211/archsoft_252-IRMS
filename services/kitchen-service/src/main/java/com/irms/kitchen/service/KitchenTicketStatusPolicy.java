package com.irms.kitchen.service;

import com.irms.kitchen.domain.KitchenTicket;
import com.irms.kitchen.domain.KitchenTicketItem;
import com.irms.kitchen.domain.TicketItemStatus;
import com.irms.kitchen.domain.TicketStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KitchenTicketStatusPolicy {

    public TicketStatus resolveStatus(KitchenTicket ticket) {
        List<KitchenTicketItem> items = ticket.getItems();

        boolean allServed = items.stream()
                .allMatch(i -> i.getStatus() == TicketItemStatus.SERVED || i.getStatus() == TicketItemStatus.CANCELLED);
        boolean allReadyOrServed = items.stream()
                .allMatch(i -> i.getStatus() == TicketItemStatus.READY || i.getStatus() == TicketItemStatus.SERVED || i.getStatus() == TicketItemStatus.CANCELLED);
        boolean anyCooking = items.stream().anyMatch(i -> i.getStatus() == TicketItemStatus.COOKING);

        if (allServed) {
            return TicketStatus.SERVED;
        }
        if (allReadyOrServed) {
            return TicketStatus.READY_TO_SERVE;
        }
        if (anyCooking && ticket.getStatus() == TicketStatus.PENDING) {
            return TicketStatus.PREPARING;
        }
        return null;
    }
}
