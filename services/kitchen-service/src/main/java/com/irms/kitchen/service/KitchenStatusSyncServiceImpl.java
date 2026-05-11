package com.irms.kitchen.service;

import com.irms.kitchen.domain.TicketItemStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KitchenStatusSyncServiceImpl implements KitchenStatusSyncService {

    private final KitchenItemStatusUpdateService itemStatusUpdateService;

    @Override
    @Transactional
    public int syncStatusByMenuItem(UUID orderId, UUID menuItemId, TicketItemStatus newStatus) {
        return itemStatusUpdateService.syncStatusByMenuItem(orderId, menuItemId, newStatus);
    }
}
