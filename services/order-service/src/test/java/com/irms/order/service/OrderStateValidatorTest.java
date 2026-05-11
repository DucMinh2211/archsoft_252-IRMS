package com.irms.order.service;

import com.irms.order.domain.OrderStatus;
import com.irms.order.exception.InvalidStateTransitionException;
import com.irms.order.validator.OrderStateValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderStateValidatorTest {

    private final OrderStateValidator validator = new OrderStateValidator();

    @Test
    void validateTransition_ShouldAllowExistingForwardTransitions() {
        assertDoesNotThrow(() -> validator.validateTransition(OrderStatus.DRAFT, OrderStatus.PENDING));
        assertDoesNotThrow(() -> validator.validateTransition(OrderStatus.PENDING, OrderStatus.COOKING));
        assertDoesNotThrow(() -> validator.validateTransition(OrderStatus.COOKING, OrderStatus.READY_TO_SERVE));
        assertDoesNotThrow(() -> validator.validateTransition(OrderStatus.READY_TO_SERVE, OrderStatus.SERVED));
    }

    @Test
    void validateTransition_ShouldAllowCompletedOrCancelledFromNonTerminalStatuses() {
        assertDoesNotThrow(() -> validator.validateTransition(OrderStatus.DRAFT, OrderStatus.COMPLETED));
        assertDoesNotThrow(() -> validator.validateTransition(OrderStatus.COOKING, OrderStatus.CANCELLED));
        assertDoesNotThrow(() -> validator.validateTransition(OrderStatus.SERVED, OrderStatus.COMPLETED));
    }

    @Test
    void validateTransition_ShouldRejectTerminalAndInvalidTransitions() {
        assertThrows(InvalidStateTransitionException.class,
                () -> validator.validateTransition(OrderStatus.COMPLETED, OrderStatus.CANCELLED));
        assertThrows(InvalidStateTransitionException.class,
                () -> validator.validateTransition(OrderStatus.CANCELLED, OrderStatus.PENDING));
        assertThrows(InvalidStateTransitionException.class,
                () -> validator.validateTransition(OrderStatus.COOKING, OrderStatus.PENDING));
    }
}
