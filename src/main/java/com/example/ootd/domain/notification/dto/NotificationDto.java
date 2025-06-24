package com.example.ootd.domain.notification.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
    String id, Instant createdAt, UUID receiverId,
    String title, String Content, String level
) {

}
