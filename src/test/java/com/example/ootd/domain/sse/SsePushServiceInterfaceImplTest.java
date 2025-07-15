package com.example.ootd.domain.sse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.entity.Notification;
import com.example.ootd.domain.notification.mapper.NotificationMapper;
import com.example.ootd.domain.notification.repository.NotificationRepository;
import com.example.ootd.domain.sse.service.SsePushServiceInterfaceImpl;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@ExtendWith(MockitoExtension.class)
class SsePushServiceInterfaceImplTest {

  private NotificationRepository repository;
  private NotificationMapper notificationMapper;
  private TaskExecutor syncExecutor;
  private SsePushServiceInterfaceImpl service;

  @BeforeEach
  void setUp() {
    repository = mock(NotificationRepository.class, RETURNS_DEEP_STUBS);
    notificationMapper = mock(NotificationMapper.class, RETURNS_DEEP_STUBS);

    syncExecutor = new SyncTaskExecutor();
    service = new SsePushServiceInterfaceImpl(repository, notificationMapper, syncExecutor);
  }

  static class TestEmitter extends SseEmitter {

    final List<Object> events = new CopyOnWriteArrayList<>();

    TestEmitter() {
      super(Long.MAX_VALUE);
    }

    @Override
    public synchronized void send(SseEventBuilder builder) throws IOException {
      events.add(builder);
    }

    @Override
    public synchronized void send(Object object) throws IOException {
      events.add(object);
    }
  }

  @Test
  void subscribe_withLastEvent_fetchesMissed() {

    UUID receiverId = UUID.randomUUID();
    UUID lastEventId = UUID.randomUUID();

    Instant lastCreatedAt = Instant.now().minus(1, ChronoUnit.MINUTES);
    Notification lastNotification = mock(Notification.class);
    when(lastNotification.getCreatedAt()).thenReturn(lastCreatedAt);

    Notification firstMissed = mock(Notification.class);
    Notification secondMissed = mock(Notification.class);
    List<Notification> missed = Arrays.asList(firstMissed, secondMissed);

    when(repository.findById(lastEventId)).thenReturn(Optional.of(lastNotification));
    when(repository.findAllByReceiverIdAndCreatedAtGreaterThanOrderByCreatedAtAsc(receiverId,
        lastCreatedAt))
        .thenReturn(missed);

    NotificationDto dto1 = mock(NotificationDto.class);
    when(dto1.id()).thenReturn(UUID.randomUUID());

    NotificationDto dto2 = mock(NotificationDto.class);
    when(dto2.id()).thenReturn(UUID.randomUUID());

    when(notificationMapper.toDto(firstMissed)).thenReturn(dto1);
    when(notificationMapper.toDto(secondMissed)).thenReturn(dto2);

    SseEmitter emitter = service.subscribe(receiverId, lastEventId);

    assertNotNull(emitter);

    verify(repository).findById(lastEventId);
    verify(repository)
        .findAllByReceiverIdAndCreatedAtGreaterThanOrderByCreatedAtAsc(receiverId, lastCreatedAt);
    verify(notificationMapper, times(missed.size())).toDto(any(Notification.class));
  }

  @Test
  void push_sendsNotificationToSubscribers() throws Exception {

    UUID receiverId = UUID.randomUUID();
    UUID notificationId = UUID.randomUUID();

    NotificationDto dto = mock(NotificationDto.class);
    when(dto.receiverId()).thenReturn(receiverId);
    when(dto.id()).thenReturn(notificationId);

    TestEmitter emitter = new TestEmitter();
    @SuppressWarnings("unchecked")
    ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> emitters =
        (ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>>)
            ReflectionTestUtils.getField(service, "emitters");

    emitters.computeIfAbsent(receiverId, k -> new CopyOnWriteArrayList<>()).add(emitter);

    service.push(dto);

    assertEquals(1, emitter.events.size(), "이벤트 1회 전송");
  }

  @Test
  void push_withNoSubscribers_doesNothing() {
    UUID receiverId = UUID.randomUUID();
    NotificationDto dto = mock(NotificationDto.class);
    when(dto.receiverId()).thenReturn(receiverId);

    assertDoesNotThrow(() -> service.push(dto));
  }
}