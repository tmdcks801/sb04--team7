package com.example.ootd.domain.notification.service;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.enums.NotificationLevel;
import java.util.UUID;

public interface NotificationServiceInterface {

  NotificationDto createNotification(UUID receiverId, String title,
      String contents, NotificationLevel level);

  NotificationDto get(UUID id);

  NotificationDto makeRead(UUID id);

}
