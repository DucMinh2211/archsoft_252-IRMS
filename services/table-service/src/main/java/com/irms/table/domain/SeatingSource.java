package com.irms.table.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum SeatingSource {
    RESERVATION,
    WAITLIST,
    WALK_IN;

    @JsonCreator
    public static SeatingSource from(String value) {
        if (value == null) {
            return null;
        }
        return SeatingSource.valueOf(value.trim().toUpperCase());
    }
}
