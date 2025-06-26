package com.example.ootd.domain.notification.service.impli;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.dto.NotificationRequest;
import com.example.ootd.domain.notification.service.inter.NotificationPublisherInterface;
import com.example.ootd.domain.notification.service.inter.NotificationServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationPublisherImpl implements NotificationPublisherInterface {

  //원래 카프카 설정
  //private final KafkaTemplate<String, NotificationDto> kafka;
//  private final EmitterRepository emitters;
//
//  @Value("${spring.kafka.template.default-topic:notification-events}")
//  private String topic;

  private final ApplicationEventPublisher eventPublisher;
  private final NotificationServiceInterface notificationService;

  @Override
  @Async("notificationExecutor")
  public void publish(NotificationRequest req) {
    NotificationDto dto = notificationService.createNotification(req);
    eventPublisher.publishEvent(dto);
  }

}
