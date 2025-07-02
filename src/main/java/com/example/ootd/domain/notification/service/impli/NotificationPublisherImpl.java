package com.example.ootd.domain.notification.service.impli;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.dto.NotificationEvent;
import com.example.ootd.domain.notification.dto.NotificationRequest;
import com.example.ootd.domain.notification.service.inter.NotificationPublisherInterface;
import com.example.ootd.domain.notification.service.inter.NotificationServiceInterface;
import com.example.ootd.domain.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
@Slf4j
public class NotificationPublisherImpl implements NotificationPublisherInterface {

  //원래 카프카 설정
  //private final KafkaTemplate<String, NotificationDto> kafka;
//  private final EmitterRepository emitters;
//
//  @Value("${spring.kafka.template.default-topic:notification-events}")
//  private String topic;

  private final ApplicationEventPublisher eventPublisher;
  private final NotificationServiceInterface notificationService;
  private final UserRepository userRepository;

  @Override
  @Async("notificationExecutor")
  @Retryable(
      retryFor = Exception.class,          // 일단 모든 예외
      maxAttempts = 3,                      // 실패시 일단은 두번 추가 시도, 1,2,4초 텀 두고
      recover = "recoverNotification",
      backoff = @Backoff(delay = 1_000,
          multiplier = 2.0,
          maxDelay = 10_000)
  )
  @Transactional //이벤트 발행, 다른 서비스에서 이벤트 발생시킬떄 이거쓰면 됨
  public void publish(NotificationRequest req) {
    NotificationDto dto = notificationService.createNotification(req);
    eventPublisher.publishEvent(dto);
  }

  @Override
  @Async("notificationExecutor")
  @Retryable(
      retryFor = Exception.class,          // 일단 모든 예외
      maxAttempts = 3,                      // 실패시 일단은 두번 추가 시도, 1,2,4초 텀 두고
      recover = "recoverNotification",
      backoff = @Backoff(delay = 1_000,
          multiplier = 2.0,
          maxDelay = 10_000)
  )
  @Transactional
  public void publishToMany(NotificationEvent event, List<UUID> userIdList) {
    userIdList.stream()
        .map(id -> new NotificationRequest(
            id,
            event.title(),
            event.content(),
            event.level()))
        .forEach(req -> {
          NotificationDto dto = notificationService.createNotification(req);
          eventPublisher.publishEvent(dto);
        });
  }

  @Override
  @Async("notificationExecutor")
  @Retryable(
      retryFor = Exception.class,          // 일단 모든 예외
      maxAttempts = 3,                      // 실패시 일단은 두번 추가 시도, 1,2,4초 텀 두고
      recover = "recoverNotification",
      backoff = @Backoff(delay = 1_000,
          multiplier = 2.0,
          maxDelay = 10_000)
  )
  @Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
  public void publishToAll(NotificationEvent event) {

    UUID cursor = null;
    boolean hasMore;

    do {
      Pageable pageable = PageRequest.of(0, 1000, Sort.by(Sort.Direction.ASC, "id"));
      List<UUID> userIds = userRepository.findIdsAfter(cursor, pageable);

      userIds.forEach(id -> {
        NotificationRequest req = new NotificationRequest(
            id,
            event.title(),
            event.content(),
            event.level()
        );
        try {
          NotificationDto dto = notificationService.createNotification(req);
          eventPublisher.publishEvent(dto);
        } catch (Exception e) {
          log.error("Notification publish failed for userId={}", id, e);
        }
      });

      if (userIds.isEmpty()) {
        hasMore = false;
      } else {
        cursor = userIds.get(userIds.size() - 1);
        hasMore = userIds.size() == 1000;
      }
    } while (hasMore);
  }

  @Recover
  public void recoverNotification(Exception ex, NotificationRequest req) {
    log.error("알림 만들기에러 발생", ex, req);
  }

}
