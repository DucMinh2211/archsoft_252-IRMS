package com.irms.order.infrastructure.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class KitchenItemStatusMapperTest {

    private final KitchenItemStatusMapper mapper = new KitchenItemStatusMapper();

    @Test
    void toKitchenStatus_ShouldPreserveExistingMappings() {
        assertEquals("PENDING", mapper.toKitchenStatus("PENDING"));
        assertEquals("COOKING", mapper.toKitchenStatus("COOKING"));
        assertEquals("READY", mapper.toKitchenStatus("READY_TO_SERVE"));
        assertEquals("SERVED", mapper.toKitchenStatus("SERVED"));
        assertEquals("CANCELLED", mapper.toKitchenStatus("CANCELLED"));
    }

    @Test
    void toKitchenStatus_ShouldReturnNullForUnknownStatus() {
        assertNull(mapper.toKitchenStatus("UNKNOWN"));
    }
}
