package com.example.ootd.domain.feed.service.cache;

import com.example.ootd.domain.feed.dto.data.FeedDto;
import com.example.ootd.domain.feed.dto.request.FeedSearchCondition;
import com.example.ootd.domain.feed.entity.Feed;
import com.example.ootd.domain.feed.mapper.FeedMapper;
import com.example.ootd.domain.feed.repository.FeedRepository;
import com.example.ootd.dto.PageResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "feed")
public class FeedCacheService {

  private final FeedRepository feedRepository;
  private final FeedMapper feedMapper;

  @Cacheable(key = "'feedList:' + #condition.toSimpleKye()")
  @Transactional(readOnly = true)
  public PageResponse<FeedDto> findCachedFeedDtos(FeedSearchCondition condition) {

    List<Feed> feeds = feedRepository.findByCondition(condition);

    boolean hasNext = (feeds.size() > condition.limit());
    String nextCursor = null;
    UUID nextIdAfter = null;
    long totalCount = feedRepository.countByCondition(condition);

    // 다음 페이지 있는 경우
    if (hasNext) {
      feeds.remove(feeds.size() - 1);   // 다음 페이지 확인용 마지막 요소 삭제
      Feed lastFeed = feeds.get(feeds.size() - 1);
      nextCursor = getNextCursor(lastFeed, condition.sortBy());
      nextIdAfter = lastFeed.getId();
    }

    List<FeedDto> feedDtos = feedMapper.toDto(feeds); // likedByMe 없이 공용 캐시용 DTO 생성

    return PageResponse.<FeedDto>builder()
        .data(feedDtos)
        .hasNext(hasNext)
        .nextCursor(nextCursor)
        .nextIdAfter(nextIdAfter)
        .sortBy(condition.sortBy())
        .sortDirection(condition.sortDirection())
        .totalCount(totalCount)
        .build();
  }

  // 캐시 삭제
  @CacheEvict(allEntries = true)
  public void evictFeedCache() {
  }

  // nextCursor 세팅
  private String getNextCursor(Feed feed, String sortBy) {
    switch (sortBy) {
      case "createdAt":
        return feed.getCreatedAt().toString();
      case "likeCount":
        return String.valueOf(feed.getLikeCount());
      default:
        return null;
    }
  }
}
