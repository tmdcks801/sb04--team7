package com.example.ootd.domain.feed.service.cache;

import com.example.ootd.domain.feed.repository.FeedLikeRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

// user 개인이 좋아요한 피드 목록 관리
@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "feed_like")
public class FeedLikeCacheService {

  private final CacheManager cacheManager;
  private final FeedLikeRepository feedLikeRepository;

  // 좋아요한 피드 id 전체 조회 (검색에 유리한 map 반환)
  @Cacheable(key = "#userId")
  public Map<String, Boolean> getFeedLikeMapByUserId(UUID userId) {

    List<UUID> likedFeedIds = feedLikeRepository.findFeedIdsByUserId(userId);
    return likedFeedIds.stream().collect(Collectors.toMap(UUID::toString, id -> true));
  }

  // feed_like_map의 #user_id 강제 갱신, 좋아요 등록/취소 시 사용
  public void refreshFeedLikeMap(UUID userId) {

    Cache cache = cacheManager.getCache("feed_like");

    if (cache != null) {

      List<UUID> likedFeedIds = feedLikeRepository.findFeedIdsByUserId(userId);
      Map<String, Boolean> likedMap = likedFeedIds.stream()
          .collect(Collectors.toMap(UUID::toString, id -> true));

      cache.put(userId, likedMap);
    }
  }
}
