package com.example.ootd.domain.feed.scheduler;

import com.example.ootd.domain.feed.dto.data.FeedSetStatesDto;
import com.example.ootd.domain.feed.entity.Feed;
import com.example.ootd.domain.feed.repository.FeedRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 좋아요 수, 댓글 수는 실제 값과 달라도 크리티컬하지 않음
// 동시성 제어보단 스케쥴러 통해 정기적으로 데이터 업데이트 하는 방식 선택
@Slf4j
@Component
@RequiredArgsConstructor
public class FeedScheduler {

  private final FeedRepository feedRepository;

  // 피드 좋아요 수, 댓글 수 업데이트
  @Transactional
  @Scheduled(cron = "0 0 0,12 * * ?") // 0, 12시 실행
  public void updateFeedStats() {

    log.debug("피드 댓글수/좋아요수 업데이트 시작");

    List<FeedSetStatesDto> feedSetStatesDtos = feedRepository.countLikesAndComments();

    for (FeedSetStatesDto dto : feedSetStatesDtos) {
      Feed feed = dto.feed();

      // 현재 피드의 좋아요 수와 실제 좋아요 수가 다르면 업데이트
      if (feed.getLikeCount() != dto.currentLikeCount()) {
        feed.updateLikeCount(dto.currentLikeCount());
      }
      // 현재 피드의 댓글 수와 실제 댓글 수가 다르면 업데이트
      if (feed.getCommentCount() != dto.currentCommentCount()) {
        feed.updateCommentCount((int) dto.currentCommentCount());
      }
    }

    log.info("피드 댓글수/좋아요수 업데이트 완료");
  }
}
