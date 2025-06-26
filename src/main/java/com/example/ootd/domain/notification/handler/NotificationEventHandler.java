package com.example.ootd.domain.notification.handler;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.sse.service.SsePushServiceInterface;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@AllArgsConstructor
@Component
public class NotificationEventHandler {

  private final SsePushServiceInterface ssePushServiceInterface;

  @EventListener//구독중인거에 알림 보내기
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(NotificationDto dto) {
    ssePushServiceInterface.push(dto);
  }
}
