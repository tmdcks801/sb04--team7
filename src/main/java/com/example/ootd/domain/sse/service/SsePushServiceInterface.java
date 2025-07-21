package com.example.ootd.domain.sse.service;

import com.example.ootd.domain.notification.dto.NotificationBulk;
import com.example.ootd.domain.notification.dto.NotificationDto;
import java.util.List;
import java.util.UUID;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SsePushServiceInterface {

  SseEmitter subscribe(UUID receiverId, UUID lastEventId);

  void push(NotificationDto notification);//핸들러에서 씀, 구독중인거한테 알림보내기


}
