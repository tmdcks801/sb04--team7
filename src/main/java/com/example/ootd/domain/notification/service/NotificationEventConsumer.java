package com.example.ootd.domain.notification.service;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.dto.NotificationRequest;
import com.example.ootd.domain.notification.entity.Notification;
import com.example.ootd.domain.notification.enums.NotificationLevel;
import com.example.ootd.domain.notification.mapper.NotificationMapper;
import com.example.ootd.domain.notification.repository.NotificationRepository;
import com.example.ootd.domain.notification.sse.EmitterRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {


  private final EmitterRepository emitters;
  private final NotificationServiceInterface notificationService;
  private final NotificationMapper notificationMapper;


  @KafkaListener(topics = "notification-events", containerFactory = "kafkaListenerContainerFactory")
  @Transactional //일단은 동기임
  public void onMessage(NotificationDto dto, Acknowledgment ack) {
    try {
      notificationService.createNotification(dto);
      emitters.send(dto.receiverId(), dto);
      ack.acknowledge();
    } catch (Exception ex) {
      log.error("Failed to push SSE for notification {}", dto.id(), ex);
      throw ex;
    }
  }


}
