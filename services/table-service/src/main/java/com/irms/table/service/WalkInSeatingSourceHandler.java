package com.irms.table.service;

import com.irms.table.domain.SeatingSource;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WalkInSeatingSourceHandler implements SeatingSourceHandler {

    @Override
    public SeatingSource source() {
        return SeatingSource.WALK_IN;
    }

    @Override
    public void handle(UUID sourceId) {
        // Walk-ins have no upstream reservation or waitlist state to update.
    }
}
