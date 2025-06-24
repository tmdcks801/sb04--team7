package com.example.ootd.domain.notification.service;

import com.example.ootd.domain.notification.dto.NotificationDto;
import javax.management.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.jmx.export.notification.UnableToSendNotificationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationPublisherImpli implements NotificationPublisherInterface {

  private final KafkaTemplate<String, NotificationDto> kafkaTemplate;

  @Value("${spring.kafka.template.default-topic:notification-events}")
  private String topic;

  @Override
  public void publish(NotificationDto notification) {
    kafkaTemplate.send(topic, notification.receiverId().toString(), notification);
  }

}
