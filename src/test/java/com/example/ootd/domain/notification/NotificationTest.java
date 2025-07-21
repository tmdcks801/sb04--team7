package com.example.ootd.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.dto.NotificationRequest;
import com.example.ootd.domain.notification.entity.Notification;
import com.example.ootd.domain.notification.enums.NotificationLevel;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationTest {

  @Test
  void builder_sets_all_fields() {

    UUID id = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    Instant createdAt = Instant.now();

    Notification notification = Notification.builder()
        .id(id)
        .receiverId(receiverId)
        .title("제목")
        .content("내용")
        .level(NotificationLevel.INFO)
        .createdAt(createdAt)
        .build();

    assertThat(notification.getId()).isEqualTo(id);
    assertThat(notification.getReceiverId()).isEqualTo(receiverId);
    assertThat(notification.getTitle()).isEqualTo("제목");
    assertThat(notification.getContent()).isEqualTo("내용");
    assertThat(notification.getLevel()).isEqualTo(NotificationLevel.INFO);
    assertThat(notification.getCreatedAt()).isEqualTo(createdAt);
  }


  @Test
  void createNotification_from_request() {

    UUID receiverId = UUID.randomUUID();
    NotificationRequest request = new NotificationRequest(
        receiverId, "제목", "내용", NotificationLevel.WARNING);

    Instant before = Instant.now();

    Notification notification = Notification.createNotification(request);

    Instant after = Instant.now();

    assertThat(notification.getId()).isNotNull();
    assertThat(notification.getReceiverId()).isEqualTo(receiverId);
    assertThat(notification.getTitle()).isEqualTo("제목");
    assertThat(notification.getContent()).isEqualTo("내용");
    assertThat(notification.getLevel()).isEqualTo(NotificationLevel.WARNING);
    assertThat(notification.getCreatedAt()).isBetween(
        before.minusSeconds(1), after.plusSeconds(1));
  }
}