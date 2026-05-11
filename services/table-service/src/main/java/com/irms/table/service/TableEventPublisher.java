package com.irms.table.service;

public interface TableEventPublisher {
    void broadcast(String eventName, Object payload);
}
