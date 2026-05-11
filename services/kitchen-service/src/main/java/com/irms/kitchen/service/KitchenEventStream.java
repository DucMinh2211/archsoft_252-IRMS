package com.irms.kitchen.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface KitchenEventStream {
    SseEmitter register();
}
