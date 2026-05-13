package com.irms.order.service;

import com.irms.order.domain.OrderItemStatus;
import com.irms.order.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

@Component
public class OrderItemStatusTransitionPolicy {

    public void validateTransition(OrderItemStatus currentStatus, OrderItemStatus newStatus) {
        if (currentStatus == OrderItemStatus.SERVED || currentStatus == OrderItemStatus.CANCELLED) {
            throw new InvalidStateTransitionException("Cannot transition item from terminal state " + currentStatus);
        }
        if (newStatus == OrderItemStatus.CANCELLED) {
            return;
        }

        boolean isValid = switch (currentStatus) {
            case PENDING -> newStatus == OrderItemStatus.COOKING || newStatus == OrderItemStatus.READY_TO_SERVE || newStatus == OrderItemStatus.SERVED;
            case COOKING -> newStatus == OrderItemStatus.READY_TO_SERVE || newStatus == OrderItemStatus.SERVED;
            case READY_TO_SERVE -> newStatus == OrderItemStatus.SERVED;
            default -> false;
        };

        if (!isValid) {
            throw new InvalidStateTransitionException("Cannot transition item from " + currentStatus + " to " + newStatus);
        }
    }
}
