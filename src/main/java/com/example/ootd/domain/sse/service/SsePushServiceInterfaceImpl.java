package com.example.ootd.domain.sse.service;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.entity.Notification;
import com.example.ootd.domain.notification.mapper.NotificationMapper;
import com.example.ootd.domain.notification.repository.NotificationRepository;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class SsePushServiceInterfaceImpl implements SsePushServiceInterface {

  private final NotificationRepository repository;
  private final NotificationMapper notificationMapper;


  private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> emitters
      = new ConcurrentHashMap<>();

  private static final long TIMEOUT = Duration.ofMinutes(30).toMillis();

  @Override //로그인할때 쓰면 될거같음, 타이밍 맞는지는 합치고 생각
  public SseEmitter subscribe(UUID receiverId, UUID lastEventId) {
    SseEmitter emitter = new SseEmitter(TIMEOUT);
    addEmitter(receiverId, emitter);

    emitter.onCompletion(() -> removeEmitter(receiverId, emitter));
    emitter.onTimeout(() -> removeEmitter(receiverId, emitter));
    emitter.onError(e -> removeEmitter(receiverId, emitter));

    if (lastEventId != null) {
      List<Notification> missed = repository
          .findAllByReceiverIdAndIdGreaterThanOrderByCreatedAtAsc(receiverId, lastEventId);

      missed.stream()
          .map(notificationMapper::toDto)
          .forEach(dto -> sendNotification(emitter, dto));
    }

    sendHeartbeat(emitter);

    return emitter;
  }

  @Override
  public void push(NotificationDto dto) { //핸들러에서 씀, 구독중인거한테 알림보내기
    CopyOnWriteArrayList<SseEmitter> list = emitters.get(dto.receiverId());
    if (list == null) {
      return;
    }

    list.forEach(em -> sendNotification(em, dto));
  }


  //알림보내기
  private void sendNotification(SseEmitter emitter, NotificationDto dto) {
    try {
      emitter.send(SseEmitter.event()
          .id(dto.id().toString())
          .name("notification")
          .data(dto));
    } catch (IOException ex) {
      emitter.completeWithError(ex);
    }
  }

  //연결 살리기
  private void sendHeartbeat(SseEmitter emitter) {
    try {
      emitter.send(SseEmitter.event()
          .name("heartbeat")
          .data("ping"));
    } catch (IOException ignore) {
    }
  }

  //더하기
  private void addEmitter(UUID receiverId, SseEmitter emitter) {
    emitters.computeIfAbsent(receiverId, k -> new CopyOnWriteArrayList<>())
        .add(emitter);
  }

  //뺴기
  private void removeEmitter(UUID receiverId, SseEmitter emitter) {
    CopyOnWriteArrayList<SseEmitter> list = emitters.get(receiverId);
    if (list != null) {
      list.remove(emitter);
    }
  }
}