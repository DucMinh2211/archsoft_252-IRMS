package com.irms.kitchen.service;

import com.irms.kitchen.domain.TicketItemStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderItemStatusMapperTest {

    private final OrderItemStatusMapper mapper = new OrderItemStatusMapper();

    @Test
    void toOrderItemStatus_ShouldPreserveExistingMappings() {
        assertEquals("PENDING", mapper.toOrderItemStatus(TicketItemStatus.PENDING));
        assertEquals("COOKING", mapper.toOrderItemStatus(TicketItemStatus.COOKING));
        assertEquals("READY_TO_SERVE", mapper.toOrderItemStatus(TicketItemStatus.READY));
        assertEquals("SERVED", mapper.toOrderItemStatus(TicketItemStatus.SERVED));
        assertEquals("CANCELLED", mapper.toOrderItemStatus(TicketItemStatus.CANCELLED));
    }
}
