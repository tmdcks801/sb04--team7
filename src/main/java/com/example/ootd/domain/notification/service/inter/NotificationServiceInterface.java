package com.example.ootd.domain.notification.service.inter;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.dto.NotificationRequest;
import com.example.ootd.dto.PageResponse;
import java.util.UUID;

public interface NotificationServiceInterface {

  NotificationDto createNotification(NotificationDto request);

  NotificationDto createNotification(NotificationRequest req);

  NotificationDto get(UUID NotificationId);

  PageResponse getPageNation(UUID receiverId, String cursor, int limit);

  void readNotification(UUID NotificationId);

}
