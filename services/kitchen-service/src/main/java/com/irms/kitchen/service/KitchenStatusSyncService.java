package com.irms.kitchen.service;

import com.irms.kitchen.domain.TicketItemStatus;

import java.util.UUID;

public interface KitchenStatusSyncService {
    int syncStatusByMenuItem(UUID orderId, UUID menuItemId, TicketItemStatus newStatus);
}
