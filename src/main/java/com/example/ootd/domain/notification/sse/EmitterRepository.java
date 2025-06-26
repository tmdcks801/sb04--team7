package com.example.ootd.domain.notification.sse;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class EmitterRepository {

  private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

  public SseEmitter getOrCreate(UUID userId) {
    return emitters.computeIfAbsent(userId, k -> newEmitter());
  }

  private SseEmitter newEmitter() {
    SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
    emitter.onCompletion(() -> emitters.values().remove(emitter));
    emitter.onTimeout(() -> emitters.values().remove(emitter));
    emitter.onError(e -> emitters.values().remove(emitter));
    return emitter;
  }

  public void send(UUID userId, Object data) {
    SseEmitter emitter = emitters.get(userId);
    if (emitter != null) {
      try {
        emitter.send(SseEmitter.event()
            .name("notification")
            .data(data)
            .id(UUID.randomUUID().toString()));  // 재연결용 lastEventId
      } catch (IOException ex) {
        emitters.remove(userId);   // 끊긴 연결 정리
      }
    }
  }
}
