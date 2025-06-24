package com.example.ootd.domain.notification.entity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
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
@CompoundIndex(def = "{'ownerId': 1, 'isRead': 1}")
public class Notification {

  @Id
  private ObjectId id;
  @Indexed
  private UUID ownerId;
  @CreatedDate
  private Instant createdAt;// 몽고db에는 localdate타임이 없어서 instant 썼음
  private NotificationType type;
  private String content;
  private boolean read;


  private Notification(UUID ownerId, NotificationType type, String content) {
    this.ownerId = ownerId;
    this.type = type;
    this.content = content;
    this.read = false;
  }

  public static Notification createRoleChangeNotification(UUID ownerId, String content) {
    return new Notification(ownerId, NotificationType.ROLE_CHANGED, content);
  }

  public static Notification createNewClothesAtribureNotification(UUID ownerId, String content) {
    return new Notification(ownerId, NotificationType.NEW_CLOTHES_ATTRIBUTE, content);
  }

  public static Notification createFeedNotification(UUID ownerId, String content) {
    return new Notification(ownerId, NotificationType.FEED_NOTIFICATION, content);
  }

  public static Notification createUploadFeedNotification(UUID ownerId, String content) {
    return new Notification(ownerId, NotificationType.UPLOAD_FEED, content);
  }

  public static Notification createFollowNotification(UUID ownerId, String content) {
    return new Notification(ownerId, NotificationType.FOLLOW, content);
  }

  public static Notification createDirectMessageNotification(UUID ownerId, String content) {
    return new Notification(ownerId, NotificationType.DIRECT_MESSAGE, content);
  }

  public void makeRead() {
    this.read = true;
  }

  //일단은 하루
  public boolean isOlder() {
    return createdAt.isBefore(Instant.now().minus(1, ChronoUnit.DAYS));
  }

}
