package com.irms.order.service;

import com.irms.order.domain.Order;
import com.irms.order.domain.OrderItem;
import com.irms.order.domain.OrderItemStatus;
import com.irms.order.repository.OrderItemRepository;
import com.irms.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@SuppressWarnings("null")
public class OrderItemSyncServiceImpl implements OrderItemSyncService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final OrderTotalCalculator orderTotalCalculator;
    private final OrderChangeNotifier notifier;

    public OrderItemSyncServiceImpl(OrderItemRepository orderItemRepository,
                                    OrderRepository orderRepository,
                                    OrderTotalCalculator orderTotalCalculator,
                                    OrderChangeNotifier notifier) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.orderTotalCalculator = orderTotalCalculator;
        this.notifier = notifier;
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
            notifier.itemStatusSynced(orderId, menuItemId, newStatus, updated);
        }
        return updated;
    }

    private void recalculateOrderTotal(Order order) {
        orderTotalCalculator.recalculate(order);
        orderRepository.save(order);
    }
}
