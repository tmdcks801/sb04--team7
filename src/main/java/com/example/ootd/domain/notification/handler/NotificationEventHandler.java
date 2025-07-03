package com.example.ootd.domain.notification.handler;

import com.example.ootd.domain.notification.dto.NotificationBulk;
import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.dto.NotificationRequest;
import com.example.ootd.domain.sse.service.SsePushServiceInterface;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@AllArgsConstructor
@Component
@Slf4j
public class NotificationEventHandler {

  private final SsePushServiceInterface ssePushServiceInterface;

  @Async("sseExecutor")//일단 문제 없는거 같음...
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  @Retryable(
      retryFor = Exception.class,          // 일단 모든 예외
      maxAttempts = 3,                      // 실패시 일단은 두번 추가 시도, 1,2,4초 텀 두고
      recover = "recoverHandler",
      backoff = @Backoff(delay = 1_000,
          multiplier = 2.0,
          maxDelay = 10_000)
  )
  public void handle(NotificationDto dto) {
    ssePushServiceInterface.push(dto);
  }


  //벌크로 한번에 보내면 내부적으로 하나씩 처리
  @Async("sseExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void handle(NotificationBulk bulk) {
    bulk.notificationDtoList().forEach(ssePushServiceInterface::push);//리스트에 있는거 하나씩
  }


  @Recover
  public void recoverHandler(Exception ex, NotificationDto dto) {
    log.error("알림 푸시 시 발생", ex, dto);
  }
}
