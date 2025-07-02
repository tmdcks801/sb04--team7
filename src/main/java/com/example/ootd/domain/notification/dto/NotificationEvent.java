package com.example.ootd.domain.notification.dto;

import com.example.ootd.domain.notification.enums.NotificationLevel;
import java.util.UUID;

public record NotificationEvent(
    String title,
    String content, NotificationLevel level
) {

}
