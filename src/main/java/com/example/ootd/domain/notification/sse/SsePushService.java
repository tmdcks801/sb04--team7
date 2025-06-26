package com.example.ootd.domain.notification.sse;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.entity.Notification;
import java.util.UUID;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SsePushService {

  SseEmitter subscribe(UUID receiverId);

  void push(NotificationDto notification);
}
