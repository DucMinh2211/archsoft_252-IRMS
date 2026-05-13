package com.irms.kitchen.service;

import com.irms.kitchen.domain.TicketItemStatus;
import com.irms.kitchen.exception.KitchenStateTransitionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KitchenItemStatusTransitionPolicyTest {

    private final KitchenItemStatusTransitionPolicy policy = new KitchenItemStatusTransitionPolicy();

    @Test
    void validateManualTransition_ShouldRejectServedRollback() {
        assertThrows(KitchenStateTransitionException.class,
                () -> policy.validateManualTransition(TicketItemStatus.SERVED, TicketItemStatus.COOKING));
    }

    @Test
    void validateManualTransition_ShouldAllowServedCancellation() {
        assertDoesNotThrow(() -> policy.validateManualTransition(TicketItemStatus.SERVED, TicketItemStatus.CANCELLED));
    }

    @Test
    void canApplySyncTransition_ShouldPreserveExistingSkipRules() {
        assertFalse(policy.canApplySyncTransition(TicketItemStatus.COOKING, TicketItemStatus.COOKING));
        assertFalse(policy.canApplySyncTransition(TicketItemStatus.CANCELLED, TicketItemStatus.READY));
        assertFalse(policy.canApplySyncTransition(TicketItemStatus.SERVED, TicketItemStatus.READY));
        assertTrue(policy.canApplySyncTransition(TicketItemStatus.COOKING, TicketItemStatus.READY));
        assertTrue(policy.canApplySyncTransition(TicketItemStatus.SERVED, TicketItemStatus.CANCELLED));
    }
}
