package com.irms.order.service;

import com.irms.order.domain.OrderItemStatus;
import com.irms.order.dto.OrderItemRequestDTO;
import com.irms.order.dto.OrderItemResponseDTO;

import java.util.UUID;

public interface OrderItemService {
    OrderItemResponseDTO updateOrderItem(UUID itemId, OrderItemRequestDTO itemDTO);
    OrderItemResponseDTO updateOrderItemStatus(UUID itemId, OrderItemStatus newStatus);
    /** Internal sync từ kitchen-service. Trả về số dòng đã update. */
    int syncStatusByMenuItem(UUID orderId, UUID menuItemId, OrderItemStatus newStatus);
    void deleteOrderItem(UUID itemId);
}
