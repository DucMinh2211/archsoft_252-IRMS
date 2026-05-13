package com.irms.kitchen.service;

import com.irms.kitchen.domain.StationType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StationManagerTest {

    private final StationManager stationManager = new StationManager();

    @Test
    void determineStation_ShouldUseExistingKeywordRules() {
        UUID menuItemId = UUID.randomUUID();

        assertEquals(StationType.GRILL, stationManager.determineStation(menuItemId, "BBQ burger"));
        assertEquals(StationType.FRYER, stationManager.determineStation(menuItemId, "crispy chips"));
        assertEquals(StationType.DESSERT, stationManager.determineStation(menuItemId, "ice cream cake"));
        assertEquals(StationType.DRINK, stationManager.determineStation(menuItemId, "hot coffee"));
        assertEquals(StationType.GENERAL, stationManager.determineStation(menuItemId, "house salad"));
        assertEquals(StationType.GENERAL, stationManager.determineStation(menuItemId, null));
    }
}
