package com.example.ootd.component.kafka;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEmitterRepository {

  private final ConcurrentMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

  public SseEmitter add(String key, SseEmitter emitter) {
    emitters.put(key, emitter);
    emitter.onCompletion(() -> emitters.remove(key));
    emitter.onTimeout(() -> emitters.remove(key));
    return emitter;
  }

  public void send(String key, Object data) {
    SseEmitter emitter = emitters.get(key);
    if (emitter != null) {
      try {
        emitter.send(SseEmitter.event().data(data));
      } catch (Exception ex) {
        emitters.remove(key);
      }
    }
  }
}
