package com.example.ootd.domain.sse.service;

import com.example.ootd.domain.notification.dto.NotificationBulk;
import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.entity.Notification;
import com.example.ootd.domain.notification.mapper.NotificationMapper;
import com.example.ootd.domain.notification.repository.NotificationRepository;
import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.SSE.SseException;
import com.example.ootd.exception.SSE.SsePushException;
import com.example.ootd.exception.SSE.SseRemoveError;
import com.example.ootd.exception.SSE.SseSendException;
import com.example.ootd.exception.SSE.SseSubscribeException;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@Slf4j
@RequiredArgsConstructor
public class SsePushServiceInterfaceImpl implements SsePushServiceInterface {

  private final NotificationRepository repository;
  private final NotificationMapper notificationMapper;


  private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> emitters
      = new ConcurrentHashMap<>();

  private static final long TIMEOUT = Duration.ofMinutes(30).toMillis();

  @Override //로그인할때 쓰면 될거같음, 타이밍 맞는지는 합치고 생각
  @Transactional
  public SseEmitter subscribe(UUID receiverId, UUID lastEventId) {
    try {
      SseEmitter emitter = new SseEmitter(TIMEOUT);
      addEmitter(receiverId, emitter);

      emitter.onCompletion(() -> removeEmitter(receiverId, emitter));
      emitter.onTimeout(() -> removeEmitter(receiverId, emitter));
      emitter.onError(e -> removeEmitter(receiverId, emitter));

      if (lastEventId != null) {
        repository.findById(lastEventId)
            .map(Notification::getCreatedAt)
            .ifPresent(lastCreatedAt -> {
              List<Notification> missed =
                  repository.findAllByReceiverIdAndCreatedAtGreaterThanOrderByCreatedAtAsc(
                      receiverId, lastCreatedAt);

              missed.stream()
                  .map(notificationMapper::toDto)
                  .forEach(dto -> sendNotification(emitter, dto));
            });
      }

      sendHeartbeat(emitter);
      return emitter;
    } catch (SseSubscribeException e) {
      throw new SseSubscribeException(ErrorCode.FAIL_SSE_SUBSCRIBE, e);
    }
  }

  @Override
  @Async("ssePushExecutor")
  @Transactional
  public void push(NotificationDto dto) { //핸들러에서 씀, 구독중인거한테 알림보내기
    try {
      CopyOnWriteArrayList<SseEmitter> list = emitters.get(dto.receiverId());
      if (list == null) {
        return;
      }

      list.forEach(em -> sendNotification(em, dto));
    } catch (SsePushException e) {
      throw new SsePushException(ErrorCode.FAIL_SSE_PUSH, e);
    }
  }

  //알림보내기
  private void sendNotification(SseEmitter emitter, NotificationDto dto) {
    try {
      emitter.send(SseEmitter.event()
          .id(dto.id().toString())
          .name("notification")
          .data(dto));
    } catch (IOException ex) {
      throw new SseException(ErrorCode.FAIL_SSE_PUSH, ex);
    }
  }

  //연결 살리기
  private void sendHeartbeat(SseEmitter emitter) {
    try {
      emitter.send(SseEmitter.event()
          .name("heartbeat")
          .data("ping"));
    } catch (IOException ignore) {
      throw new SseException(ErrorCode.FAIL_SSE_HEARTBEAT, ignore);
    }
  }

  //더하기
  private void addEmitter(UUID receiverId, SseEmitter emitter) {
    try {
      emitters.computeIfAbsent(receiverId, k -> new CopyOnWriteArrayList<>())
          .add(emitter);
    } catch (SseSendException e) {
      throw new SseSendException(ErrorCode.FAIL_SSE_ADD);
    }
  }

  //뺴기
  private void removeEmitter(UUID receiverId, SseEmitter emitter) {
    try {
      CopyOnWriteArrayList<SseEmitter> list = emitters.get(receiverId);
      if (list != null) {
        list.remove(emitter);
      }
    } catch (SseRemoveError e) {
      throw new SseRemoveError(ErrorCode.FAIL_SSE_REMOVE, e);
    }
  }
}