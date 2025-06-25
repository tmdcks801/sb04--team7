package com.example.ootd.domain.notification.service;


import com.example.ootd.domain.notification.dto.NotificationRequest;

public interface NotificationPublisherInterface {

  void publish(NotificationRequest event);
}
