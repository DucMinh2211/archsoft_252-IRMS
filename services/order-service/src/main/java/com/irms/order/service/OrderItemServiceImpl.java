package com.irms.order.service;

import com.irms.order.domain.Order;
import com.irms.order.domain.OrderItem;
import com.irms.order.domain.OrderItemStatus;
import com.irms.order.dto.OrderItemRequestDTO;
import com.irms.order.dto.OrderItemResponseDTO;
import com.irms.order.exception.BusinessValidationException;
import com.irms.order.exception.OrderNotFoundException;
import com.irms.order.infrastructure.client.KitchenItemStatusSyncClient;
import com.irms.order.mapper.OrderMapper;
import com.irms.order.repository.OrderItemRepository;
import com.irms.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@SuppressWarnings("null")
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final KitchenItemStatusSyncClient kitchenSyncClient;
    private final OrderTotalCalculator orderTotalCalculator;
    private final OrderItemStatusTransitionPolicy itemStatusTransitionPolicy;
    private final OrderEventPublisher eventPublisher;

    public OrderItemServiceImpl(OrderItemRepository orderItemRepository,
                                OrderRepository orderRepository,
                                OrderMapper orderMapper,
                                 KitchenItemStatusSyncClient kitchenSyncClient,
                                 OrderTotalCalculator orderTotalCalculator,
                                 OrderItemStatusTransitionPolicy itemStatusTransitionPolicy,
                                 OrderEventPublisher eventPublisher) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.kitchenSyncClient = kitchenSyncClient;
        this.orderTotalCalculator = orderTotalCalculator;
        this.itemStatusTransitionPolicy = itemStatusTransitionPolicy;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public OrderItemResponseDTO updateOrderItem(UUID itemId, OrderItemRequestDTO itemDTO) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new OrderNotFoundException("Order Item not found"));
        
        if (item.getStatus() != OrderItemStatus.PENDING) {
            throw new BusinessValidationException("Cannot update item that is already being cooked or served.");
        }

        item.setQuantity(itemDTO.getQuantity());
        item.setNote(itemDTO.getNote());
        
        OrderItem savedItem = orderItemRepository.save(item);
        recalculateOrderTotal(item.getOrder());
        
        return orderMapper.toDto(savedItem);
    }

    @Override
    @Transactional
    public OrderItemResponseDTO updateOrderItemStatus(UUID itemId, OrderItemStatus newStatus) {
        return updateOrderItemStatusInternal(itemId, newStatus, true);
    }

    /**
     * Sync từ kitchen-service: cập nhật status theo (orderId, menuItemId), không propagate ngược lại
     * để tránh loop. Hỗ trợ trường hợp 1 menu item có nhiều dòng order_item — update tất cả non-terminal.
     */
    @Override
    @Transactional
    public int syncStatusByMenuItem(UUID orderId, UUID menuItemId, OrderItemStatus newStatus) {
        List<OrderItem> items = orderItemRepository.findByOrder_IdAndMenuItemId(orderId, menuItemId);
        int updated = 0;
        for (OrderItem it : items) {
            if (it.getStatus() == newStatus) continue;
            // Bỏ qua nếu đã terminal nhưng status mới khác — không quay ngược
            if (it.getStatus() == OrderItemStatus.SERVED && newStatus != OrderItemStatus.CANCELLED) continue;
            if (it.getStatus() == OrderItemStatus.CANCELLED) continue;
            it.setStatus(newStatus);
            orderItemRepository.save(it);
            updated++;
        }
        if (updated > 0 && newStatus == OrderItemStatus.CANCELLED && !items.isEmpty()) {
            recalculateOrderTotal(items.get(0).getOrder());
        }
        if (updated > 0) {
            eventPublisher.broadcast("order.itemStatus.sync",
                    java.util.Map.of("orderId", orderId, "menuItemId", menuItemId, "status", newStatus.name(), "updated", updated));
        }
        return updated;
    }

    private OrderItemResponseDTO updateOrderItemStatusInternal(UUID itemId, OrderItemStatus newStatus, boolean propagateToKitchen) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new OrderNotFoundException("Order Item not found"));

        OrderItemStatus currentStatus = item.getStatus();
        itemStatusTransitionPolicy.validateTransition(currentStatus, newStatus);

        item.setStatus(newStatus);
        OrderItem savedItem = orderItemRepository.save(item);

        if (newStatus == OrderItemStatus.CANCELLED) {
            recalculateOrderTotal(item.getOrder());
        }

        // POS hủy món / mark served → báo kitchen để chef thấy đồng bộ.
        if (propagateToKitchen && (newStatus == OrderItemStatus.CANCELLED || newStatus == OrderItemStatus.SERVED)) {
            kitchenSyncClient.syncItemStatus(item.getOrder().getId(), item.getMenuItemId(), newStatus.name());
        }

        OrderItemResponseDTO dto = orderMapper.toDto(savedItem);
        eventPublisher.broadcast("order.itemStatus", dto);
        return dto;
    }

    @Override
    @Transactional
    public void deleteOrderItem(UUID itemId) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new OrderNotFoundException("Order Item not found"));
        
        if (item.getStatus() != OrderItemStatus.PENDING) {
            throw new BusinessValidationException("Cannot delete item that is already being cooked or served.");
        }
        
        Order order = item.getOrder();
        order.removeItem(item);
        
        orderItemRepository.delete(item);
        recalculateOrderTotal(order);
    }

    private void recalculateOrderTotal(Order order) {
        orderTotalCalculator.recalculate(order);
        orderRepository.save(order);
    }
}
