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

  //  @Async("sseSingleExecutor")//일단 문제 없는거 같음...
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = false)
//  )   //push가 비동기라 애는 비동기 필요없음
  public void handle(NotificationDto dto) {
    try {
      ssePushServiceInterface.push(dto);
    } catch (Exception ex) {
      log.error("알람 푸시 실패", dto, ex);
    }
  }


  //벌크로 한번에 보내면 내부적으로 하나씩 처리
  @Async("sseBulkExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void handle(NotificationBulk bulk) {
    bulk.notificationDtoList().forEach(this::safePush);//리스트에 있는거 하나씩, 실패시 로그만
  }

  private void safePush(NotificationDto dto) {
    try {
      ssePushServiceInterface.push(dto);
    } catch (Exception ex) {
      log.error("Bulk 도중 알람 푸시 실패", dto, ex);
    }
  }

}
