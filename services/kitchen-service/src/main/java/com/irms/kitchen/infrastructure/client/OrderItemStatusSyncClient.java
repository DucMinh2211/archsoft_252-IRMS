package com.irms.kitchen.infrastructure.client;

import java.util.UUID;

public interface OrderItemStatusSyncClient {
    void syncItemStatus(UUID orderId, UUID menuItemId, String orderItemStatus);
}
