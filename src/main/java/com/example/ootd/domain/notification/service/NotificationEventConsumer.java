package com.example.ootd.domain.notification.service;

import com.example.ootd.domain.notification.dto.NotificationDto;
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
  @Transactional
  public void onMessage(NotificationDto dto, Acknowledgment ack) {
    try {
      switch (dto.level()) {
        case "INFO" ->
            notificationService.createNotification(dto.receiverId(), dto.title(), dto.content(),
                NotificationLevel.INFO);
        case "WARNING" ->
            notificationService.createNotification(dto.receiverId(), dto.title(), dto.content(),
                NotificationLevel.WARNING);
        case "ERROR" ->
            notificationService.createNotification(dto.receiverId(), dto.title(), dto.content(),
                NotificationLevel.ERROR);
      }
      ack.acknowledge();
    } catch (Exception ex) {
      log.error("Failed to handle notification dto: {}", dto, ex);
      throw ex;
    }
  }


}
