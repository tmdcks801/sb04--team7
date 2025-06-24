package com.example.ootd.domain.notification.repository;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.entity.Notification;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, UUID> {


}
