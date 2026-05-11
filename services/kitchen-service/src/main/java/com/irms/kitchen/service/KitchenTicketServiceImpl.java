package com.irms.kitchen.service;

import com.irms.kitchen.domain.KitchenTicket;
import com.irms.kitchen.domain.KitchenTicketItem;
import com.irms.kitchen.domain.StationType;
import com.irms.kitchen.domain.TicketItemStatus;
import com.irms.kitchen.domain.TicketStatus;
import com.irms.kitchen.dto.CreateTicketRequest;
import com.irms.kitchen.exception.ResourceNotFoundException;
import com.irms.kitchen.repository.KitchenTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class KitchenTicketServiceImpl implements KitchenTicketService {

    private final KitchenTicketRepository ticketRepository;
    private final StationAssignmentPolicy stationAssignmentPolicy;
    private final KitchenTicketTimingPolicy ticketTimingPolicy;
    private final KitchenEventPublisher eventPublisher;

    @Override
    @Transactional
    public KitchenTicket createTicket(CreateTicketRequest request) {
        log.info("Creating kitchen ticket for order ID: {}", request.getOrderId());

        KitchenTicket ticket = KitchenTicket.builder()
                .orderId(request.getOrderId())
                .tableId(request.getTableId())
                .status(TicketStatus.PENDING)
                .build();

        ticket.setExpectedReadyTime(ticketTimingPolicy.calculateExpectedReadyTime(ticket));

        request.getItems().forEach(itemDto -> {
            StationType station = stationAssignmentPolicy.determineStation(
                    itemDto.getMenuItemId(),
                    itemDto.getMenuItemName()
            );
            KitchenTicketItem item = KitchenTicketItem.builder()
                    .menuItemId(itemDto.getMenuItemId())
                    .menuItemName(itemDto.getMenuItemName())
                    .quantity(itemDto.getQuantity())
                    .notes(itemDto.getNotes())
                    .station(station)
                    .status(TicketItemStatus.PENDING)
                    .build();
            ticket.addItem(item);
        });

        KitchenTicket saved = ticketRepository.save(ticket);
        eventPublisher.broadcast("ticket.created", Map.of("id", saved.getId(), "orderId", saved.getOrderId()));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public KitchenTicket getTicketById(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Kitchen ticket not found with id: " + ticketId));
    }
}
