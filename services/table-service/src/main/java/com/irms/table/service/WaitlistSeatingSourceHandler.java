package com.irms.table.service;

import com.irms.table.domain.SeatingSource;
import com.irms.table.domain.WaitlistStatus;
import com.irms.table.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WaitlistSeatingSourceHandler implements SeatingSourceHandler {

    private final WaitlistRepository waitlistRepository;

    @Override
    public SeatingSource source() {
        return SeatingSource.WAITLIST;
    }

    @Override
    public void handle(UUID sourceId) {
        if (sourceId == null) {
            return;
        }

        waitlistRepository.findById(sourceId).ifPresent(waitlistEntry -> {
            waitlistEntry.setStatus(WaitlistStatus.SEATED);
            waitlistRepository.save(waitlistEntry);
        });
    }
}
