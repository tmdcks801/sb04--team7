package com.example.ootd.domain.notification.service.inter;

import com.example.ootd.domain.notification.dto.NotificationDto;

public interface NotificationDelivererInterface {

  void process(NotificationDto event);
}
