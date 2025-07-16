package com.example.ootd.domain.notification.entity;

import static lombok.AccessLevel.PROTECTED;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.dto.NotificationRequest;
import com.example.ootd.domain.notification.enums.NotificationLevel;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "notification")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Notification {

  @MongoId(targetType = FieldType.STRING)
  private UUID id;
  private UUID receiverId;
  private Instant createdAt;// 몽고db에는 localdate타입이 없어서 instant 썼음
  private String title;
  private String content;
  private NotificationLevel level;


  @Builder
  private Notification(UUID id, UUID receiverId, String title, String content,
      NotificationLevel level, Instant createdAt) {
    this.id = id;
    this.receiverId = receiverId;
    this.content = content;
    this.title = title;
    this.level = level;
    this.createdAt = createdAt;
  }


  public static Notification createNotification(NotificationDto dto) {
    return Notification.builder()
        .id(dto.id())
        .receiverId(dto.receiverId())
        .content(dto.content())
        .title(dto.title())
        .level(dto.level())
        .createdAt(dto.createdAt())
        .build();
  }

  public static Notification createNotification(NotificationRequest req) {
    return Notification.builder()
        .id(UUID.randomUUID())
        .receiverId(req.receiverId())
        .content(req.content())
        .title(req.title())
        .level(req.level())
        .createdAt(Instant.now())
        .build();
  }

}
