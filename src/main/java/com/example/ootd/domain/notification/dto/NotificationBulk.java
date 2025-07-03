package com.example.ootd.domain.notification.dto;

import java.util.List;

public record NotificationBulk(
    List<NotificationDto> notificationDtoList
) {

}
