package com.example.ootd.domain.notification.dto;

import com.example.ootd.domain.notification.enums.NotificationLevel;
import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
    UUID id, UUID receiverId, String content,
    String title, NotificationLevel level, Instant createdAt
) {

}
