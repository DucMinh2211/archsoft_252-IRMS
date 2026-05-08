package com.irms.kitchen.service;

import com.irms.kitchen.domain.KitchenTicket;
import com.irms.kitchen.domain.KitchenTicketItem;
import com.irms.kitchen.domain.StationType;
import com.irms.kitchen.domain.TicketItemStatus;
import com.irms.kitchen.domain.TicketStatus;
import com.irms.kitchen.dto.CreateTicketRequest;
import com.irms.kitchen.exception.ResourceNotFoundException;
import com.irms.kitchen.infrastructure.client.OrderServiceClient;
import com.irms.kitchen.infrastructure.sse.SseBroadcaster;
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
public class KitchenServiceImpl implements KitchenService {

    private final KitchenTicketRepository ticketRepository;
    private final KitchenTicketItemRepository itemRepository;
    private final StationManager stationManager;
    private final OrderPrioritizer orderPrioritizer;
    private final OrderServiceClient orderServiceClient;
    private final SseBroadcaster sseBroadcaster;

    @Override
    @Transactional
    public KitchenTicket createTicket(CreateTicketRequest request) {
        log.info("Creating kitchen ticket for order ID: {}", request.getOrderId());
        
        KitchenTicket ticket = KitchenTicket.builder()
                .orderId(request.getOrderId())
                .tableId(request.getTableId())
                .status(TicketStatus.PENDING)
                .build();
                
        // Calculate expected ready time based on prioritizer
        ticket.setExpectedReadyTime(orderPrioritizer.calculateExpectedReadyTime(ticket));

        request.getItems().forEach(itemDto -> {
            StationType station = stationManager.determineStation(itemDto.getMenuItemId(), itemDto.getMenuItemName());
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
        sseBroadcaster.broadcast("ticket.created", java.util.Map.of("id", saved.getId(), "orderId", saved.getOrderId()));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<KitchenTicket> getActiveTickets() {
        return ticketRepository.findByStatusInOrderByCreatedAtAsc(
                Arrays.asList(TicketStatus.PENDING, TicketStatus.PREPARING)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public KitchenTicket getTicketById(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Kitchen ticket not found with id: " + ticketId));
    }

    @Override
    @Transactional
    public void updateItemStatus(UUID itemId, TicketItemStatus newStatus) {
        updateItemStatusInternal(itemId, newStatus, true);
    }

    /**
     * Sync từ order-service: tìm tất cả kitchen items thuộc orderId+menuItemId, update status.
     * Không propagate ngược lại để tránh loop.
     */
    @Override
    @Transactional
    public int syncStatusByMenuItem(UUID orderId, UUID menuItemId, TicketItemStatus newStatus) {
        List<KitchenTicketItem> items = itemRepository.findByTicket_OrderIdAndMenuItemId(orderId, menuItemId);
        int updated = 0;
        for (KitchenTicketItem it : items) {
            if (it.getStatus() == newStatus) continue;
            if (it.getStatus() == TicketItemStatus.CANCELLED) continue;
            it.setStatus(newStatus);
            itemRepository.save(it);
            checkAndUpdateTicketStatus(it.getTicket());
            updated++;
        }
        if (updated > 0) {
            sseBroadcaster.broadcast("ticket.itemStatus.sync",
                    java.util.Map.of("orderId", orderId, "menuItemId", menuItemId, "status", newStatus.name(), "updated", updated));
        }
        return updated;
    }

    private void updateItemStatusInternal(UUID itemId, TicketItemStatus newStatus, boolean propagateToOrder) {
        log.info("Updating status for item ID: {} to {} (propagate={})", itemId, newStatus, propagateToOrder);
        KitchenTicketItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Kitchen ticket item not found with id: " + itemId));

        item.setStatus(newStatus);
        itemRepository.save(item);

        checkAndUpdateTicketStatus(item.getTicket());

        if (propagateToOrder) {
            UUID orderId = item.getTicket().getOrderId();
            if (orderId != null) {
                String orderItemStatus = mapToOrderItemStatus(newStatus);
                if (orderItemStatus != null) {
                    orderServiceClient.syncItemStatus(orderId, item.getMenuItemId(), orderItemStatus);
                }
            }
        }

        sseBroadcaster.broadcast("ticket.itemStatus",
                java.util.Map.of("itemId", itemId, "ticketId", item.getTicket().getId(), "status", newStatus.name()));
    }

    private String mapToOrderItemStatus(TicketItemStatus s) {
        return switch (s) {
            case PENDING   -> "PENDING";
            case COOKING   -> "COOKING";
            case READY     -> "READY_TO_SERVE";
            case CANCELLED -> "CANCELLED";
        };
    }

    @Override
    @Transactional
    public void updateTicketStatus(UUID ticketId, TicketStatus newStatus) {
        log.info("Updating status for ticket ID: {} to {}", ticketId, newStatus);
        KitchenTicket ticket = getTicketById(ticketId);
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
    
    private void checkAndUpdateTicketStatus(KitchenTicket ticket) {
        List<KitchenTicketItem> items = ticket.getItems();
        
        boolean allReady = items.stream().allMatch(i -> i.getStatus() == TicketItemStatus.READY || i.getStatus() == TicketItemStatus.CANCELLED);
        boolean anyCooking = items.stream().anyMatch(i -> i.getStatus() == TicketItemStatus.COOKING);
        
        if (allReady) {
            ticket.setStatus(TicketStatus.READY_TO_SERVE);
            log.info("Ticket {} is now READY_TO_SERVE", ticket.getId());
        } else if (anyCooking && ticket.getStatus() == TicketStatus.PENDING) {
            ticket.setStatus(TicketStatus.PREPARING);
            log.info("Ticket {} is now PREPARING", ticket.getId());
        }
        
        ticketRepository.save(ticket);
    }
}
