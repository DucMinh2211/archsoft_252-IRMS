package com.irms.kitchen.service;

import com.irms.kitchen.domain.TicketItemStatus;
import com.irms.kitchen.exception.KitchenStateTransitionException;
import org.springframework.stereotype.Component;

@Component
public class KitchenItemStatusTransitionPolicy {

    public void validateManualTransition(TicketItemStatus currentStatus, TicketItemStatus newStatus) {
        if (isServedRollback(currentStatus, newStatus)) {
            throw new KitchenStateTransitionException("Không thể hoàn tác món đã được phục vụ xong.");
        }
    }

    public boolean canApplySyncTransition(TicketItemStatus currentStatus, TicketItemStatus newStatus) {
        if (currentStatus == newStatus) {
            return false;
        }
        if (currentStatus == TicketItemStatus.CANCELLED) {
            return false;
        }
        return !isServedRollback(currentStatus, newStatus);
    }

    private boolean isServedRollback(TicketItemStatus currentStatus, TicketItemStatus newStatus) {
        return currentStatus == TicketItemStatus.SERVED && newStatus != TicketItemStatus.CANCELLED;
    }
}
