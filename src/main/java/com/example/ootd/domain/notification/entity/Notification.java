package com.example.ootd.domain.notification.entity;

import static lombok.AccessLevel.PROTECTED;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notification")
@Getter
@Setter
@NoArgsConstructor(access = PROTECTED)
public class Notification {

  @Id
  private ObjectId id;
  @Indexed
  private UUID ownerId;
  @CreatedDate
  private Instant createdAt;// 몽고db에는 localdate타임이 없어서 instant 썼음
  private NotificationType type;
  private String contents;
  private boolean read;


  @Builder
  private Notification(UUID ownerId, NotificationType type, String contents) {
    this.ownerId = ownerId;
    this.type = type;
    this.contents = contents;
    this.read = false;
  }

  public static Notification createRoleChangeNotification(UUID ownerId, String contents) {
    return Notification.builder()
        .ownerId(ownerId)
        .type(NotificationType.ROLE_CHANGED)
        .contents(contents)
        .build();
  }

  public static Notification createNewClothesAtribureNotification(UUID ownerId, String contents) {
    return Notification.builder()
        .ownerId(ownerId)
        .type(NotificationType.NEW_CLOTHES_ATTRIBUTE)
        .contents(contents)
        .build();
  }

  public static Notification createFeedNotification(UUID ownerId, String contents) {
    return Notification.builder()
        .ownerId(ownerId)
        .type(NotificationType.FEED_NOTIFICATION)
        .contents(contents)
        .build();
  }

  public static Notification createUploadFeedNotification(UUID ownerId, String contents) {
    return Notification.builder()
        .ownerId(ownerId)
        .type(NotificationType.UPLOAD_FEED)
        .contents(contents)
        .build();
  }

  public static Notification createFollowNotification(UUID ownerId, String contents) {
    return Notification.builder()
        .ownerId(ownerId)
        .type(NotificationType.FOLLOW)
        .contents(contents)
        .build();
  }

  public static Notification createDirectMessageNotification(UUID ownerId, String contents) {
    return Notification.builder()
        .ownerId(ownerId)
        .type(NotificationType.DIRECT_MESSAGE)
        .contents(contents)
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
