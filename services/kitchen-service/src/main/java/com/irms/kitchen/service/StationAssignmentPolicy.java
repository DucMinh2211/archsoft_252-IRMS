package com.irms.kitchen.service;

import com.irms.kitchen.domain.StationType;

import java.util.UUID;

public interface StationAssignmentPolicy {
    StationType determineStation(UUID menuItemId, String menuItemName);
}
