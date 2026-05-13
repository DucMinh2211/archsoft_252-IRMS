package com.irms.kitchen.service;

import com.irms.kitchen.domain.KitchenTicket;
import com.irms.kitchen.domain.TicketStatus;
import com.irms.kitchen.repository.KitchenTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KitchenTicketStatusUpdater {

    private final KitchenTicketStatusPolicy ticketStatusPolicy;
    private final KitchenTicketRepository ticketRepository;

    public void refreshStatus(KitchenTicket ticket) {
        TicketStatus resolvedStatus = ticketStatusPolicy.resolveStatus(ticket);

        if (resolvedStatus == TicketStatus.SERVED) {
            ticket.setStatus(TicketStatus.SERVED);
            log.info("Ticket {} is now SERVED", ticket.getId());
        } else if (resolvedStatus == TicketStatus.READY_TO_SERVE) {
            ticket.setStatus(TicketStatus.READY_TO_SERVE);
            log.info("Ticket {} is now READY_TO_SERVE", ticket.getId());
        } else if (resolvedStatus == TicketStatus.PREPARING && ticket.getStatus() == TicketStatus.PENDING) {
            ticket.setStatus(TicketStatus.PREPARING);
            log.info("Ticket {} is now PREPARING", ticket.getId());
        }

        ticketRepository.save(ticket);
    }
}
