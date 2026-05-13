package com.irms.table.service;

import com.irms.table.domain.WaitlistEntry;
import com.irms.table.domain.WaitlistStatus;
import com.irms.table.exception.TableBusinessException;
import org.springframework.stereotype.Component;

@Component
public class WaitlistStatusPolicy {

    public void validateCanNotify(WaitlistEntry entry) {
        if (entry.getStatus() != WaitlistStatus.WAITING) {
            throw new TableBusinessException("Chỉ có thể thông báo cho khách đang ở trạng thái WAITING");
        }
    }

    public void validateCanSeat(WaitlistEntry entry) {
        if (entry.getStatus() == WaitlistStatus.SEATED || entry.getStatus() == WaitlistStatus.LEFT) {
            throw new TableBusinessException("Khách này đã được xếp bàn hoặc đã rời đi");
        }
    }
}
