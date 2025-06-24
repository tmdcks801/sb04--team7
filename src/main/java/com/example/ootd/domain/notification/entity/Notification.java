package com.example.ootd.domain.notification.entity;

import static lombok.AccessLevel.PROTECTED;

import com.example.ootd.domain.notification.enums.NotificationLevel;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "notification")
@Getter
@CompoundIndex(def = "{'receiverId':1, 'read':1}", name = "receiver_read_idx")
@NoArgsConstructor(access = PROTECTED)
public class Notification {

  @MongoId(targetType = FieldType.STRING)
  private UUID id;
  @Indexed
  private UUID receiverId;
  @CreatedDate
  private Instant createdAt;// 몽고db에는 localdate타입이 없어서 instant 썼음
  private String title;
  private String contents;
  private NotificationLevel level;
  private boolean read;


  @Builder
  private Notification(UUID receiverId, String title, String contents, NotificationLevel level) {
    this.id = UUID.randomUUID();
    this.receiverId = receiverId;
    this.contents = contents;
    this.title = title;
    this.level = level;
    this.read = false;
  }

  public static Notification createInfoNotification(UUID receiverId, String title,
      String contents) {
    return Notification.builder()
        .receiverId(receiverId)
        .contents(contents)
        .level(NotificationLevel.INFO)
        .build();
  }

  public static Notification createWarningNotification(UUID receiverId, String title,
      String contents) {
    return Notification.builder()
        .receiverId(receiverId)
        .contents(contents)
        .level(NotificationLevel.WARNING)
        .build();
  }

  public static Notification createErrorNotification(UUID receiverId, String title,
      String contents) {
    return Notification.builder()
        .receiverId(receiverId)
        .contents(contents)
        .level(NotificationLevel.ERROR)
        .build();
  }


  public void makeRead() {
    this.read = true;
  }

  //일단은 하루
  public boolean isOlder() {
    return createdAt.isBefore(Instant.now().minus(1, ChronoUnit.DAYS));
  }

}
