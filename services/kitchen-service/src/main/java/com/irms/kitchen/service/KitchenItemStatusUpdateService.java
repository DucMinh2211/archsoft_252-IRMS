package com.irms.kitchen.service;

import com.irms.kitchen.domain.KitchenTicketItem;
import com.irms.kitchen.domain.TicketItemStatus;
import com.irms.kitchen.exception.ResourceNotFoundException;
import com.irms.kitchen.infrastructure.client.OrderItemStatusSyncClient;
import com.irms.kitchen.repository.KitchenTicketItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KitchenItemStatusUpdateService {

    private final KitchenTicketItemRepository itemRepository;
    private final OrderItemStatusSyncClient orderServiceClient;
    private final OrderItemStatusMapper orderItemStatusMapper;
    private final KitchenItemStatusTransitionPolicy transitionPolicy;
    private final KitchenTicketStatusUpdater ticketStatusUpdater;
    private final KitchenEventPublisher eventPublisher;

    @Transactional
    public void updateItemStatus(UUID itemId, TicketItemStatus newStatus) {
        log.info("Updating status for item ID: {} to {} (propagate={})", itemId, newStatus, true);
        KitchenTicketItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Kitchen ticket item not found with id: " + itemId));

        transitionPolicy.validateManualTransition(item.getStatus(), newStatus);
        item.setStatus(newStatus);
        itemRepository.save(item);

        ticketStatusUpdater.refreshStatus(item.getTicket());
        syncOrderItemStatus(item, newStatus);

        eventPublisher.broadcast("ticket.itemStatus",
                Map.of("itemId", itemId, "ticketId", item.getTicket().getId(), "status", newStatus.name()));
    }

    @Transactional
    public int syncStatusByMenuItem(UUID orderId, UUID menuItemId, TicketItemStatus newStatus) {
        List<KitchenTicketItem> items = itemRepository.findByTicket_OrderIdAndMenuItemId(orderId, menuItemId);
        int updated = 0;
        for (KitchenTicketItem item : items) {
            if (!transitionPolicy.canApplySyncTransition(item.getStatus(), newStatus)) {
                continue;
            }

            item.setStatus(newStatus);
            itemRepository.save(item);
            ticketStatusUpdater.refreshStatus(item.getTicket());
            updated++;
        }
        if (updated > 0) {
            eventPublisher.broadcast("ticket.itemStatus.sync",
                    Map.of("orderId", orderId, "menuItemId", menuItemId, "status", newStatus.name(), "updated", updated));
        }
        return updated;
    }

    private void syncOrderItemStatus(KitchenTicketItem item, TicketItemStatus newStatus) {
        UUID orderId = item.getTicket().getOrderId();
        if (orderId == null) {
            return;
        }

        String orderItemStatus = orderItemStatusMapper.toOrderItemStatus(newStatus);
        if (orderItemStatus != null) {
            orderServiceClient.syncItemStatus(orderId, item.getMenuItemId(), orderItemStatus);
        }
    }
}
