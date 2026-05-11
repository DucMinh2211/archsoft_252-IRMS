package com.irms.kitchen.service;

import com.irms.kitchen.domain.KitchenTicket;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OrderPrioritizer implements KitchenTicketTimingPolicy {

    private static final int DEFAULT_PREP_TIME_MINUTES = 20;

    /**
     * Calculates the expected ready time for a ticket based on its items
     * or standard SLA configurations.
     */
    @Override
    public LocalDateTime calculateExpectedReadyTime(KitchenTicket ticket) {
        // In a complex scenario, this would evaluate all items and find the max prep time
        // Here we use a standard 20 minutes from creation/now.
        LocalDateTime baseTime = ticket.getCreatedAt() != null ? ticket.getCreatedAt() : LocalDateTime.now();
        return baseTime.plusMinutes(DEFAULT_PREP_TIME_MINUTES);
    }
    
    /**
     * Determines if a ticket is breached (past its expected ready time).
     */
    @Override
    public boolean isTicketBreached(KitchenTicket ticket) {
        if (ticket.getExpectedReadyTime() == null) return false;
        return LocalDateTime.now().isAfter(ticket.getExpectedReadyTime());
    }
    
    /**
     * Determines if a ticket is at risk of breaching SLA (e.g. within 5 minutes).
     */
    @Override
    public boolean isTicketAtRisk(KitchenTicket ticket) {
        if (ticket.getExpectedReadyTime() == null) return false;
        LocalDateTime warningTime = ticket.getExpectedReadyTime().minusMinutes(5);
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(warningTime) && now.isBefore(ticket.getExpectedReadyTime());
    }
}
