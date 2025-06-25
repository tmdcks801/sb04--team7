package com.example.ootd.domain.notification.service;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.dto.NotificationRequest;
import javax.management.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.jmx.export.notification.UnableToSendNotificationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationPublisherImpl implements NotificationPublisherInterface {

  private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;

  @Value("${spring.kafka.template.default-topic:notification-events}")
  private String topic;

  @Override//이거 이미 비동기  알림 만들떄 이거 쓰면 됨
  public void publish(NotificationRequest notification) {
    kafkaTemplate.send(topic, notification.receiverId().toString(), notification);
  }

}
