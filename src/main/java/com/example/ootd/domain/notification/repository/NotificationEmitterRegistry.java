package com.example.ootd.domain.notification.repository;

import java.util.Collection;
import java.util.UUID;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationEmitterRegistry<SseEmitter> {

  SseEmitter subscribe(UUID userId, @Nullable String lastEventId);

  Collection<SseEmitter> getEmitters(UUID userId);

  void remove(SseEmitter emitter);

  void removeAll(UUID userId);
}
