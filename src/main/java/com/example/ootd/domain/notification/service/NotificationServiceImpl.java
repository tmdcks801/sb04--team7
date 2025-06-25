package com.example.ootd.domain.notification.service;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.dto.NotificationRequest;
import com.example.ootd.domain.notification.entity.Notification;
import com.example.ootd.domain.notification.mapper.NotificationMapper;
import com.example.ootd.domain.notification.repository.NotificationRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationServiceInterface {

  private final NotificationRepository repository;
  private final NotificationMapper notificationMapper;

  @Override
  @Transactional
  public NotificationDto createNotification(NotificationRequest request) {
    Notification notification = Notification.createNotification(request.receiverId()
        , request.contents(), request.title(), request.level());
    repository.save(notification);

    return notificationMapper.toDto(notification);
  }

  @Override
  @Transactional
  public NotificationDto get(UUID notificationId) {
    Notification notification = repository.findById(notificationId).orElseThrow(() ->
        new IllegalArgumentException("Notification not found: " + notificationId));
    ;
    return notificationMapper.toDto(notification);
  }

  @Override
  @Transactional
  public NotificationDto makeRead(UUID notificationId) {
    Notification notification = repository.findById(notificationId).orElseThrow(() ->
        new IllegalArgumentException("Notification not found: " + notificationId));
    notification.makeRead();
    return notificationMapper.toDto(notification);
  }
}
