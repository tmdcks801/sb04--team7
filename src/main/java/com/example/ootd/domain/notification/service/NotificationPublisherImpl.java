package com.example.ootd.domain.notification.service;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.dto.NotificationRequest;
import com.example.ootd.domain.notification.entity.Notification;
import com.example.ootd.domain.notification.mapper.NotificationMapper;
import com.example.ootd.domain.notification.repository.NotificationRepository;
import com.example.ootd.domain.notification.sse.EmitterRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationPublisherImpl implements NotificationPublisherInterface {

  private final KafkaTemplate<String, NotificationDto> kafka;
  private final EmitterRepository emitters;

  @Value("${spring.kafka.template.default-topic:notification-events}")
  private String topic;

  @Override//이거 이미 비동기  알림 만들떄 이거 쓰면 됨
  public void publish(NotificationRequest req) {
    NotificationDto dto = new NotificationDto(
        UUID.randomUUID(),
        Instant.now(),
        req.receiverId(),// createdAt
        req.content(),
        req.title(),
        req.level()
    );
    kafka.send(topic, dto.receiverId().toString(), dto);

    emitters.send(dto.receiverId(), dto);
  }

}
