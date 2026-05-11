package com.irms.kitchen.service;

import com.irms.kitchen.domain.StationType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class StationManager {

    private static final List<StationRule> STATION_RULES = List.of(
            new StationRule(StationType.GRILL, List.of("grill", "steak", "burger", "bbq")),
            new StationRule(StationType.FRYER, List.of("fry", "crispy", "chips")),
            new StationRule(StationType.DESSERT, List.of("cake", "ice cream", "sweet", "dessert")),
            new StationRule(StationType.DRINK, List.of("drink", "juice", "coffee", "tea", "cola", "beer"))
    );

    /**
     * Determines the appropriate station for a given menu item.
     * In a real application, this might query the menu-service or a local cache
     * to get category information for the menu item.
     */
    public StationType determineStation(UUID menuItemId, String menuItemName) {
        if (menuItemName == null) return StationType.GENERAL;
        
        String lowerName = menuItemName.toLowerCase();

        for (StationRule rule : STATION_RULES) {
            if (rule.matches(lowerName)) {
                return rule.station();
            }
        }
        
        return StationType.GENERAL;
    }

    private record StationRule(StationType station, List<String> keywords) {
        private boolean matches(String menuItemName) {
            return keywords.stream().anyMatch(menuItemName::contains);
        }
    }
}
