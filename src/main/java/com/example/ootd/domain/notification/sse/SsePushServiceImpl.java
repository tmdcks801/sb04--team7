package com.example.ootd.domain.notification.sse;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.entity.Notification;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class SsePushServiceImpl implements SsePushService {

  private static final long DEFAULT_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(30);

  private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> emitters =
      new ConcurrentHashMap<>();

  @Override
  public SseEmitter subscribe(UUID receiverId) {
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MS);

    emitters.computeIfAbsent(receiverId, id -> new CopyOnWriteArrayList<>())
        .add(emitter);
    Runnable cleanup = () -> removeEmitter(receiverId, emitter);
    emitter.onCompletion(cleanup);
    emitter.onTimeout(cleanup);
    emitter.onError(ex -> cleanup.run());

    try {
      emitter.send(SseEmitter.event()
          .name("connected")
          .data("OK")
          .id("0"));
    } catch (IOException e) {

      cleanup.run();
    }
    return emitter;
  }

  @Override
  public void push(NotificationDto dto) {
    UUID receiverId = dto.receiverId();
    List<SseEmitter> list = emitters.get(receiverId);

    // 구독자가 없으면 바로 반환
    if (list == null || list.isEmpty()) {
      return;
    }

    List<SseEmitter> dead = new ArrayList<>(list.size());

    // CopyOnWriteArrayList 라 ConcurrentModificationException 은 안 나지만,
    // 실패 emitter 따로 모아 한 번에 제거 → 성능 + 가독성 ↑
    for (SseEmitter emitter : list) {
      try {
        emitter.send(SseEmitter.event()
            .id(dto.id().toString())   // 커서 역할
            .name("notification")
            .data(dto));
      } catch (IOException | IllegalStateException ex) {
        // 네트워크 끊김 · 타임아웃 · 이미 완료된 emitter
        dead.add(emitter);
      }
    }

    // 죽은 emitter 정리
    if (!dead.isEmpty()) {
      list.removeAll(dead);
      if (list.isEmpty()) {
        emitters.remove(receiverId);   // 메모리 누수 방지
      }
    }
  }


  private void removeEmitter(UUID receiverId, SseEmitter emitter) {
    List<SseEmitter> list = emitters.get(receiverId);
    if (list == null) {
      return;
    }
    list.remove(emitter);
    if (list.isEmpty()) {
      emitters.remove(receiverId);
    }
  }
}
