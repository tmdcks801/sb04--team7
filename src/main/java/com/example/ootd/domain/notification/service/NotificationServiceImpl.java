package com.example.ootd.domain.notification.service;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.dto.NotificationRequest;
import com.example.ootd.domain.notification.entity.Notification;
import com.example.ootd.domain.notification.mapper.NotificationMapper;
import com.example.ootd.domain.notification.repository.NotificationRepository;
import com.example.ootd.dto.PageResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
  @Transactional(readOnly = true)
  public NotificationDto get(UUID notificationId) {
    Notification notification = repository.findById(notificationId).orElseThrow(() ->
        new IllegalArgumentException("Notification 없음 " + notificationId));
    ;
    return notificationMapper.toDto(notification);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse getPageNation(UUID receiverId, String cursor, int limit) {
    
    List<Notification> slice;
    if (cursor == null || cursor.isBlank()) {
      slice = repository.findByReceiverIdOrderByCreatedAtDesc(
          receiverId,
          PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
      );
    } else {
      Instant before = Instant.parse(cursor);
      slice = repository.findByReceiverIdAndCreatedAtBeforeOrderByCreatedAtDesc(
          receiverId,
          before,
          PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
      );
    }

    List<NotificationDto> dtos = slice.stream()
        .map(notificationMapper::toDto)
        .toList();

    boolean hasNext = dtos.size() == limit;
    String nextCursor = null;
    UUID nextIdAfter = null;

    if (hasNext) {
      NotificationDto last = dtos.get(dtos.size() - 1);
      nextCursor = last.createdAt().toString();
      nextIdAfter = last.id();
    }
    int totalCount = repository.countByReceiverId(receiverId);
    return new PageResponse(
        dtos,
        hasNext,
        nextCursor,
        nextIdAfter,
        "createdAt",
        "DESC",
        totalCount
    );
  }

  @Override
  @Transactional
  public void readNotification(UUID notificationId) {
    Notification notification = repository.findById(notificationId).orElseThrow(() ->
        new IllegalArgumentException("Notification 없음 " + notificationId));
    repository.delete(notification);
  }
}
