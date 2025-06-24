package com.example.ootd.domain.notification.repository;

import com.example.ootd.domain.notification.dto.NotificationDto;

public interface NotificationPublisher {

  void publishNotification(NotificationDto event);
}
