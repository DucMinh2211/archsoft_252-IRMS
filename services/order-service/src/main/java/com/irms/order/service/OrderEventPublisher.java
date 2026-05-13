package com.irms.order.service;

public interface OrderEventPublisher {
    void broadcast(String eventName, Object payload);
}
