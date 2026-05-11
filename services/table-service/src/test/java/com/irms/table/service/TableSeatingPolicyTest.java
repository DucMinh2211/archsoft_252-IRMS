package com.irms.table.service;

import com.irms.table.domain.RestaurantTable;
import com.irms.table.domain.TableStatus;
import com.irms.table.exception.TableBusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TableSeatingPolicyTest {

    private final TableSeatingPolicy policy = new TableSeatingPolicy();

    @Test
    void validateCanSeat_ShouldAllowAvailableOrReservedTables() {
        assertDoesNotThrow(() -> policy.validateCanSeat(table(TableStatus.AVAILABLE)));
        assertDoesNotThrow(() -> policy.validateCanSeat(table(TableStatus.RESERVED)));
    }

    @Test
    void validateCanSeat_ShouldRejectOtherStatuses() {
        assertThrows(TableBusinessException.class, () -> policy.validateCanSeat(table(TableStatus.OCCUPIED)));
    }

    @Test
    void validateMoveRules_ShouldPreserveExistingStatusChecks() {
        assertDoesNotThrow(() -> policy.validateMoveSource(table(TableStatus.OCCUPIED)));
        assertDoesNotThrow(() -> policy.validateMoveDestination(table(TableStatus.AVAILABLE)));
        assertThrows(TableBusinessException.class, () -> policy.validateMoveSource(table(TableStatus.AVAILABLE)));
        assertThrows(TableBusinessException.class, () -> policy.validateMoveDestination(table(TableStatus.RESERVED)));
    }

    private RestaurantTable table(TableStatus status) {
        RestaurantTable table = new RestaurantTable();
        table.setTableNumber("T1");
        table.setStatus(status);
        return table;
    }
}
