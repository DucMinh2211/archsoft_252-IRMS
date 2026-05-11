package com.irms.kitchen.service;

import com.irms.kitchen.domain.KitchenTicket;
import com.irms.kitchen.domain.KitchenTicketItem;
import com.irms.kitchen.domain.StationType;
import com.irms.kitchen.domain.TicketItemStatus;
import com.irms.kitchen.domain.TicketStatus;
import com.irms.kitchen.repository.KitchenTicketItemRepository;
import com.irms.kitchen.repository.KitchenTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class KdsServiceImpl implements KdsService {

    private final KitchenTicketRepository ticketRepository;
    private final KitchenTicketItemRepository itemRepository;
    private final KitchenTicketService kitchenTicketService;
    private final KitchenItemStatusUpdateService itemStatusUpdateService;

    @Override
    @Transactional(readOnly = true)
    public List<KitchenTicket> getActiveTickets() {
        return ticketRepository.findByStatusInOrderByCreatedAtAsc(
                Arrays.asList(TicketStatus.PENDING, TicketStatus.PREPARING)
        );
    }

    @Override
    @Transactional
    public void updateItemStatus(UUID itemId, TicketItemStatus newStatus) {
        itemStatusUpdateService.updateItemStatus(itemId, newStatus);
    }

    @Override
    @Transactional
    public void updateTicketStatus(UUID ticketId, TicketStatus newStatus) {
        log.info("Updating status for ticket ID: {} to {}", ticketId, newStatus);
        KitchenTicket ticket = kitchenTicketService.getTicketById(ticketId);
        ticket.setStatus(newStatus);
        ticketRepository.save(ticket);

        // TODO: If cancelled, maybe cascade cancel to items
    }

    @Override
    @Transactional(readOnly = true)
    public List<KitchenTicketItem> getItemsByStation(StationType station) {
        return itemRepository.findByStationAndStatusInOrderByCreatedAtAsc(
                station,
                Arrays.asList(TicketItemStatus.PENDING, TicketItemStatus.COOKING)
        );
    }
}
