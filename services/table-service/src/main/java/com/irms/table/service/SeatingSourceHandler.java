package com.irms.table.service;

import com.irms.table.domain.SeatingSource;

import java.util.UUID;

public interface SeatingSourceHandler {
    SeatingSource source();

    void handle(UUID sourceId);

    default boolean supports(SeatingSource source) {
        return source() == source;
    }
}
