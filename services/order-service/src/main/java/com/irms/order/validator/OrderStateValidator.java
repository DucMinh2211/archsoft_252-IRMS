package com.irms.order.validator;

import com.irms.order.domain.OrderStatus;
import com.irms.order.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

@Component
public class OrderStateValidator {

    public void validateTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        // Cho phép kết thúc đơn (COMPLETED hoặc CANCELLED) từ bất kỳ trạng thái không terminal.
        // Phù hợp luồng POS thực: thanh toán có thể xảy ra bất cứ lúc nào (takeaway, cash trước, v.v.)
        if (currentStatus == OrderStatus.COMPLETED || currentStatus == OrderStatus.CANCELLED) {
            throw new InvalidStateTransitionException(
                    String.format("Cannot transition order from terminal state %s", currentStatus)
            );
        }
        if (newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.CANCELLED) {
            return;
        }

        boolean isValid = switch (currentStatus) {
            case DRAFT -> newStatus == OrderStatus.PENDING;
            case PENDING -> newStatus == OrderStatus.COOKING;
            case COOKING -> newStatus == OrderStatus.READY_TO_SERVE;
            case READY_TO_SERVE -> newStatus == OrderStatus.SERVED;
            case SERVED -> false; // Từ SERVED chỉ đi tiếp đến COMPLETED (đã handle ở trên)
            default -> false;
        };

        if (!isValid) {
            throw new InvalidStateTransitionException(
                    String.format("Cannot transition order from %s to %s", currentStatus, newStatus)
            );
        }
    }
}
