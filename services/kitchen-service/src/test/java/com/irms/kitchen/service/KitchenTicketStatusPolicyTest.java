package com.irms.kitchen.service;

import com.irms.kitchen.domain.KitchenTicket;
import com.irms.kitchen.domain.KitchenTicketItem;
import com.irms.kitchen.domain.TicketItemStatus;
import com.irms.kitchen.domain.TicketStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class KitchenTicketStatusPolicyTest {

    private final KitchenTicketStatusPolicy policy = new KitchenTicketStatusPolicy();

    @Test
    void resolveStatus_ShouldMarkServedWhenAllItemsServedOrCancelled() {
        KitchenTicket ticket = ticket(TicketStatus.PREPARING, TicketItemStatus.SERVED, TicketItemStatus.CANCELLED);

        assertEquals(TicketStatus.SERVED, policy.resolveStatus(ticket));
    }

    @Test
    void resolveStatus_ShouldMarkReadyWhenAllItemsReadyServedOrCancelled() {
        KitchenTicket ticket = ticket(TicketStatus.PREPARING, TicketItemStatus.READY, TicketItemStatus.SERVED);

        assertEquals(TicketStatus.READY_TO_SERVE, policy.resolveStatus(ticket));
    }

    @Test
    void resolveStatus_ShouldMarkPreparingWhenPendingTicketHasCookingItem() {
        KitchenTicket ticket = ticket(TicketStatus.PENDING, TicketItemStatus.PENDING, TicketItemStatus.COOKING);

        assertEquals(TicketStatus.PREPARING, policy.resolveStatus(ticket));
    }

    @Test
    void resolveStatus_ShouldReturnNullWhenNoRuleApplies() {
        KitchenTicket ticket = ticket(TicketStatus.PREPARING, TicketItemStatus.PENDING, TicketItemStatus.COOKING);

        assertNull(policy.resolveStatus(ticket));
    }

    private KitchenTicket ticket(TicketStatus ticketStatus, TicketItemStatus... itemStatuses) {
        KitchenTicket ticket = new KitchenTicket();
        ticket.setStatus(ticketStatus);
        ticket.setItems(new ArrayList<>());
        for (TicketItemStatus status : itemStatuses) {
            KitchenTicketItem item = new KitchenTicketItem();
            item.setStatus(status);
            ticket.addItem(item);
        }
        return ticket;
    }
}
