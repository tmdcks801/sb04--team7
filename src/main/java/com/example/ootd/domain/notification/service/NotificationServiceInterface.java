package com.example.ootd.domain.notification.service;

import com.example.ootd.domain.notification.dto.NotificationDto;
import java.util.UUID;

public interface NotificationServiceInterface {

  NotificationDto createInfoNotification(UUID receiverId, String title,
      String contents);

  NotificationDto createWarningNotification(UUID receiverId, String title,
      String contents);

  NotificationDto createErrorNotification(UUID receiverId, String title,
      String contents);

  NotificationDto get(UUID id);

  NotificationDto makeRead(UUID id);

}
