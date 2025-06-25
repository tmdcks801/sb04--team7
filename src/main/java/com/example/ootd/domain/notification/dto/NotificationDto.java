package com.example.ootd.domain.notification.dto;

import com.example.ootd.domain.notification.enums.NotificationLevel;
import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
    UUID id, Instant createdAt, UUID receiverId,
    String title, String content, NotificationLevel level
) {

}
