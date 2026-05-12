package com.irms.order.service;

import com.irms.order.domain.OrderItemStatus;

import java.util.UUID;

public interface OrderItemSyncService {
    int syncStatusByMenuItem(UUID orderId, UUID menuItemId, OrderItemStatus newStatus);
}
