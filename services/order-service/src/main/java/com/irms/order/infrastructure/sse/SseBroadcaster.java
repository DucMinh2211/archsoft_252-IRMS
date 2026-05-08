package com.irms.order.infrastructure.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory SSE registry: giữ danh sách emitter đang mở, gửi event tới tất cả.
 * Đơn instance — không dùng được nếu chạy multi-replica (cần Redis pub/sub).
 */
@Slf4j
@Component
public class SseBroadcaster {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter register() {
        SseEmitter emitter = new SseEmitter(0L); // không timeout
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("hello").data(Map.of("service", "order-service")));
        } catch (IOException ignored) {
            emitters.remove(emitter);
        }
        return emitter;
    }

    public void broadcast(String eventName, Object payload) {
        for (SseEmitter e : emitters) {
            try {
                e.send(SseEmitter.event().name(eventName).data(payload));
            } catch (Exception ex) {
                emitters.remove(e);
            }
        }
    }

    /** Heartbeat mỗi 25s để giữ connection sống qua proxy (tránh idle timeout). */
    @Scheduled(fixedRate = 25000)
    public void heartbeat() {
        for (SseEmitter e : emitters) {
            try {
                e.send(SseEmitter.event().name("ping").data("."));
            } catch (Exception ex) {
                emitters.remove(e);
            }
        }
    }
}
