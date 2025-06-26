package com.example.ootd.domain.notification.service.inter;


import com.example.ootd.domain.notification.dto.NotificationRequest;

public interface NotificationPublisherInterface {

  void publish(NotificationRequest event);
}
