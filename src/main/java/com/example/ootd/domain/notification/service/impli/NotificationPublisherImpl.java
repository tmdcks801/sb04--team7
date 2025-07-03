package com.example.ootd.domain.notification.service.impli;

import com.example.ootd.domain.notification.dto.NotificationBulk;
import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.dto.NotificationEvent;
import com.example.ootd.domain.notification.dto.NotificationRequest;
import com.example.ootd.domain.notification.service.inter.NotificationPublisherInterface;
import com.example.ootd.domain.notification.service.inter.NotificationServiceInterface;
import com.example.ootd.domain.user.repository.UserRepository;
import java.util.ArrayList;
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
  private final int pageSize = 600; //bulk안에 들어갈 수
  private final int batchSize = 600;


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

    List<NotificationDto> batch = new ArrayList<>(batchSize);
    for (UUID id : userIdList) {
      NotificationRequest req = new NotificationRequest(
          id, event.title(),
          event.content(), event.level());

      NotificationDto dto = notificationService.createNotification(req);
      batch.add(dto);
      if (batch.size() == batchSize) {//batchSize 차면 이벤트 발행
        eventPublisher.publishEvent(new NotificationBulk(List.copyOf(batch)));
        batch = new ArrayList<>(batchSize);
      }
    }
    if (!batch.isEmpty()) {// 안차고 남은거 이벤트 발행
      eventPublisher.publishEvent(new NotificationBulk(List.copyOf(batch)));
    }
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
  @Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)//한번에 한개만 되도록하기?
  public void publishToAll(NotificationEvent event) {

    UUID cursor = null;
    boolean hasMore;

    do {
      Pageable pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.ASC, "id"));
      List<UUID> userIds = userRepository.findIdsAfter(cursor, pageable);//db oom해결?
      List<NotificationDto> dtoList = new ArrayList<>();
      userIds.forEach(id -> {
        NotificationRequest req = new NotificationRequest(
            id,
            event.title(),
            event.content(),
            event.level()
        );
        try {
          //dto 만들고 한번에 모아서 bulk로 보내기
          NotificationDto dto = notificationService.createNotification(req);
          dtoList.add(dto);
        } catch (Exception e) {
          log.error("모두에게 알림 발송중 오류", e);
        }
      });
      //한번에 최대 pageSize 까지 알람 보내기
      eventPublisher.publishEvent(new NotificationBulk(dtoList));
      if (userIds.isEmpty()) {
        hasMore = false;
      } else {
        cursor = userIds.get(userIds.size() - 1);
        hasMore = userIds.size() == pageSize;
      }
    } while (hasMore);
  }

  @Recover
  public void recoverNotification(Exception ex, NotificationRequest req) {
    log.error("알림 만들기에러 발생", ex, req);
  }

}
