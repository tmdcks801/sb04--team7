package com.example.ootd.domain.notification.service;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.dto.NotificationRequest;
import com.example.ootd.dto.PageResponse;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

public interface NotificationServiceInterface {

  NotificationDto createNotification(NotificationDto request);

  NotificationDto get(UUID NotificationId);

  PageResponse getPageNation(UUID receiverId, String cursor, int limit);

  void readNotification(UUID NotificationId);

}
