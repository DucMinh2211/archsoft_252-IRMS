package com.irms.order.service;

import com.irms.order.domain.OrderItemStatus;
import com.irms.order.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemStatusTransitionPolicyTest {

    private final OrderItemStatusTransitionPolicy policy = new OrderItemStatusTransitionPolicy();

    @Test
    void validateTransition_ShouldAllowExistingForwardTransitions() {
        assertDoesNotThrow(() -> policy.validateTransition(OrderItemStatus.PENDING, OrderItemStatus.COOKING));
        assertDoesNotThrow(() -> policy.validateTransition(OrderItemStatus.PENDING, OrderItemStatus.READY_TO_SERVE));
        assertDoesNotThrow(() -> policy.validateTransition(OrderItemStatus.PENDING, OrderItemStatus.SERVED));
        assertDoesNotThrow(() -> policy.validateTransition(OrderItemStatus.COOKING, OrderItemStatus.READY_TO_SERVE));
        assertDoesNotThrow(() -> policy.validateTransition(OrderItemStatus.COOKING, OrderItemStatus.SERVED));
        assertDoesNotThrow(() -> policy.validateTransition(OrderItemStatus.READY_TO_SERVE, OrderItemStatus.SERVED));
    }

    @Test
    void validateTransition_ShouldAllowCancellationFromNonTerminalStatuses() {
        assertDoesNotThrow(() -> policy.validateTransition(OrderItemStatus.PENDING, OrderItemStatus.CANCELLED));
        assertDoesNotThrow(() -> policy.validateTransition(OrderItemStatus.COOKING, OrderItemStatus.CANCELLED));
        assertDoesNotThrow(() -> policy.validateTransition(OrderItemStatus.READY_TO_SERVE, OrderItemStatus.CANCELLED));
    }

    @Test
    void validateTransition_ShouldRejectTerminalAndBackwardTransitions() {
        assertThrows(InvalidStateTransitionException.class,
                () -> policy.validateTransition(OrderItemStatus.SERVED, OrderItemStatus.CANCELLED));
        assertThrows(InvalidStateTransitionException.class,
                () -> policy.validateTransition(OrderItemStatus.CANCELLED, OrderItemStatus.PENDING));
        assertThrows(InvalidStateTransitionException.class,
                () -> policy.validateTransition(OrderItemStatus.COOKING, OrderItemStatus.PENDING));
    }
}
