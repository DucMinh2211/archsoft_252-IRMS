package com.irms.order.infrastructure.client;

import java.util.UUID;

public interface KitchenItemStatusSyncClient {
    void syncItemStatus(UUID orderId, UUID menuItemId, String orderItemStatus);
}
