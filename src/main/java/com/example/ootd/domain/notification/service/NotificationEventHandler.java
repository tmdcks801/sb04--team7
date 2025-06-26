package com.example.ootd.domain.notification.service;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.entity.Notification;
import com.example.ootd.domain.notification.repository.NotificationRepository;
import com.example.ootd.domain.notification.sse.SsePushService;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class NotificationEventHandler {

  private final SsePushService ssePushService;   // 이미 구현돼 있다고 가정

  @EventListener
  public void handle(NotificationDto dto) {
    ssePushService.push(dto);   // emitter.send(...)
  }
}
