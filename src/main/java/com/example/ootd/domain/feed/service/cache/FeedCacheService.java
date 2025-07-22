package com.example.ootd.domain.feed.service.cache;

import com.example.ootd.domain.feed.dto.data.FeedCountDto;
import com.example.ootd.domain.feed.entity.Feed;
import com.example.ootd.domain.feed.repository.FeedRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

// 피드 좋아요/댓글 개수 관리
@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "feed")
public class FeedCacheService {

  private final FeedRepository feedRepository;
  private final CacheManager cacheManager;

  @Cacheable(key = "#feedId")
  public FeedCountDto getFeedStates(UUID feedId) {
    return feedRepository.countLikeAndCommentByFeedId(feedId);
  }

  public Map<UUID, FeedCountDto> getFeedStates(List<UUID> feedIds) {

    Cache cache = cacheManager.getCache("feed");
    Map<UUID, FeedCountDto> result = new HashMap<>();

    List<UUID> missingFeedIds = new ArrayList<>();
    for (UUID feedId : feedIds) {
      FeedCountDto dto = cache.get(feedId, FeedCountDto.class);
      if (dto != null) {
        result.put(feedId, dto);
      } else {
        missingFeedIds.add(feedId);
      }
    }

    if (!missingFeedIds.isEmpty()) {
      List<FeedCountDto> feedCountDtos = feedRepository.countLikesAndCommentsByFeedIds(
          missingFeedIds);
      for (FeedCountDto dto : feedCountDtos) {
        cache.put(dto.feedId(), dto);
        result.put(dto.feedId(), dto);
      }
    }

    return result;
  }

  @CacheEvict(key = "#feedId")
  public void evictFeedCache(UUID feedId) {
  }

  // 좋아요 수 증가
  public void likeCountIncrease(Feed feed) {

    Cache cache = cacheManager.getCache("feed");

    if (cache == null) {
      log.warn("Feed cache is not available");
      return;
    }

    FeedCountDto currentFeedCountDto = cache.get(feed.getId(), FeedCountDto.class);

    long likeCount = feed.getLikeCount();
    long commentCount = feed.getCommentCount();
    if (currentFeedCountDto != null) {
      likeCount = currentFeedCountDto.currentLikeCount();
      commentCount = currentFeedCountDto.currentCommentCount();
    }
    FeedCountDto updatedFeedCountDto = new FeedCountDto(feed.getId(), likeCount + 1, commentCount);

    cache.put(feed.getId(), updatedFeedCountDto);
  }

  // 좋아요 수 감소
  public void likeCountDecrease(Feed feed) {

    Cache cache = cacheManager.getCache("feed");

    if (cache == null) {
      log.warn("Feed cache is not available");
      return;
    }

    FeedCountDto currentFeedCountDto = cache.get(feed.getId(), FeedCountDto.class);

    long likeCount = feed.getLikeCount();
    long commentCount = feed.getCommentCount();
    if (currentFeedCountDto != null) {
      likeCount = currentFeedCountDto.currentLikeCount();
      commentCount = currentFeedCountDto.currentCommentCount();
    }
    FeedCountDto updatedFeedCountDto = new FeedCountDto(feed.getId(), likeCount - 1, commentCount);

    cache.put(feed.getId(), updatedFeedCountDto);
  }

  // 댓글 수 증가
  public void commentCountIncrease(Feed feed) {

    Cache cache = cacheManager.getCache("feed");

    if (cache == null) {
      log.warn("Feed cache is not available");
      return;
    }

    FeedCountDto currentFeedCountDto = cache.get(feed.getId(), FeedCountDto.class);

    long likeCount = feed.getLikeCount();
    long commentCount = feed.getCommentCount();
    if (currentFeedCountDto != null) {
      likeCount = currentFeedCountDto.currentLikeCount();
      commentCount = currentFeedCountDto.currentCommentCount();
    }
    FeedCountDto updatedFeedCountDto = new FeedCountDto(feed.getId(), likeCount, commentCount + 1);

    cache.put(feed.getId(), updatedFeedCountDto);
  }
}
