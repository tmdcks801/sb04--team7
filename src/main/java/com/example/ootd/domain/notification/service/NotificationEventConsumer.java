package com.example.ootd.domain.notification.service;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.dto.NotificationRequest;
import com.example.ootd.domain.notification.entity.Notification;
import com.example.ootd.domain.notification.enums.NotificationLevel;
import com.example.ootd.domain.notification.repository.NotificationRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

  private final NotificationServiceInterface notificationService;


  @KafkaListener(topics = "notification-events", containerFactory = "kafkaListenerContainerFactory")
  @Transactional //일단은 동기임
  public void onMessage(NotificationRequest request, Acknowledgment ack) {
    try {
      notificationService.createNotification(request);
      ack.acknowledge();
    } catch (Exception ex) {
      //로그로 에러 던지기
      throw ex;
    }
  }


}
