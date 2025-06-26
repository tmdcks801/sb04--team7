package com.example.ootd.domain.notification.repository;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, UUID> {

  List<Notification> findByReceiverIdAndCreatedAtBeforeOrderByCreatedAtDesc(
      UUID receiverId, Instant before, Pageable pageable);

  List<Notification> findByReceiverIdOrderByCreatedAtDesc(
      UUID receiverId, Pageable pageable);

  int countByReceiverId(UUID receiverId);

  List<Notification> findAllByReceiverIdAndIdGreaterThanOrderByCreatedAtAsc
      (UUID receiverId, UUID lastEventId);

}
