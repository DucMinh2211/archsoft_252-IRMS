package com.irms.kitchen.service;

import com.irms.kitchen.domain.KitchenTicket;

import java.time.LocalDateTime;

public interface KitchenTicketTimingPolicy {
    LocalDateTime calculateExpectedReadyTime(KitchenTicket ticket);
    boolean isTicketBreached(KitchenTicket ticket);
    boolean isTicketAtRisk(KitchenTicket ticket);
}
