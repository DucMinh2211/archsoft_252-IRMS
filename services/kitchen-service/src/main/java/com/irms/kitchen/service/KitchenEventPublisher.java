package com.irms.kitchen.service;

public interface KitchenEventPublisher {
    void broadcast(String eventName, Object payload);
}
