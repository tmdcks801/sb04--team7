package com.example.ootd.domain.notification.service;

import com.example.ootd.domain.notification.dto.NotificationDto;

public interface NotificationPublisherInterface {

  void publish(NotificationDto event);
}
