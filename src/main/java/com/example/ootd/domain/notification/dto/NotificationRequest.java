package com.example.ootd.domain.notification.dto;

import com.example.ootd.domain.notification.enums.NotificationLevel;
import java.util.UUID;
import lombok.Builder;

@Builder
public record NotificationRequest(UUID receiverId, String title,
                                  String content, NotificationLevel level) {

}
