package com.irms.table.service;

import com.irms.table.domain.WaitlistEntry;
import com.irms.table.domain.WaitlistStatus;
import com.irms.table.exception.TableBusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WaitlistStatusPolicyTest {

    private final WaitlistStatusPolicy policy = new WaitlistStatusPolicy();

    @Test
    void validateCanNotify_ShouldOnlyAllowWaitingEntries() {
        assertDoesNotThrow(() -> policy.validateCanNotify(entry(WaitlistStatus.WAITING)));
        assertThrows(TableBusinessException.class, () -> policy.validateCanNotify(entry(WaitlistStatus.NOTIFIED)));
    }

    @Test
    void validateCanSeat_ShouldRejectTerminalEntries() {
        assertDoesNotThrow(() -> policy.validateCanSeat(entry(WaitlistStatus.WAITING)));
        assertDoesNotThrow(() -> policy.validateCanSeat(entry(WaitlistStatus.NOTIFIED)));
        assertThrows(TableBusinessException.class, () -> policy.validateCanSeat(entry(WaitlistStatus.SEATED)));
        assertThrows(TableBusinessException.class, () -> policy.validateCanSeat(entry(WaitlistStatus.LEFT)));
    }

    private WaitlistEntry entry(WaitlistStatus status) {
        WaitlistEntry entry = new WaitlistEntry();
        entry.setStatus(status);
        return entry;
    }
}
