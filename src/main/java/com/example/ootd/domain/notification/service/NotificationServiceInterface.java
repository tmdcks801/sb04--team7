package com.example.ootd.domain.notification.service;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.dto.NotificationRequest;
import com.example.ootd.domain.notification.enums.NotificationLevel;
import java.util.UUID;

public interface NotificationServiceInterface {

  NotificationDto createNotification(NotificationRequest request);

  NotificationDto get(UUID NotificationId);

  NotificationDto makeRead(UUID NotificationId);

}
