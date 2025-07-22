package com.example.ootd.domain.feed.config;

import com.example.ootd.domain.feed.scheduler.FeedScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedStatesStartupRunner {

  private final FeedScheduler feedScheduler;

  // 어플리케이션 실행 시 피드 상태 업데이트
  @EventListener(ApplicationReadyEvent.class)
  public void onReady() {
    feedScheduler.updateFeedStats();
  }
}
