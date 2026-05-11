package com.irms.payment.infrastructure.client;

import java.util.UUID;

public interface OrderCompletionClient {
    void updateOrderStatusToCompleted(UUID orderId);
}
