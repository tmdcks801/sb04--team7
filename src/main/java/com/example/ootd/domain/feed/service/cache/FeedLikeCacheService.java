package com.example.ootd.domain.feed.service.cache;

import com.example.ootd.domain.feed.repository.FeedLikeRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "feed_like")
public class FeedLikeCacheService {

  private final FeedLikeRepository feedLikeRepository;
  private final CacheManager cacheManager;

  // 캐시 조회
  @Cacheable(key = "#feedId")
  public long getLikeCount(UUID feedId) {
    return feedLikeRepository.countByFeedId(feedId);
  }

  // 캐시 갱신
  @CachePut(key = "#feedId")
  public long updateLikeCount(UUID feedId) {
    return feedLikeRepository.countByFeedId(feedId);
  }

  public Map<UUID, Long> getLikeCountMap(List<UUID> feedIds) {

    Map<UUID, Long> likeCountMap = new HashMap<>();

    // 캐시에서 조회
    for (UUID feedId : feedIds) {
      Long cachedCount = cacheManager.getCache("feed_like")
          .get(feedId, Long.class);
      if (cachedCount != null) {
        likeCountMap.put(feedId, cachedCount);
      }
    }

    // 캐시에 없는 ID 추출
    List<UUID> missedIds = feedIds.stream()
        .filter(id -> !likeCountMap.containsKey(id))
        .toList();

    // 쿼리로 일괄 조회 + 캐시에 저장
    if (!missedIds.isEmpty()) {
      List<Object[]> results = feedLikeRepository.countLikesByFeedIds(missedIds);

      for (Object[] row : results) {
        UUID feedId = (UUID) row[0];
        Long count = (Long) row[1];
        likeCountMap.put(feedId, count);
        cacheManager.getCache("feed_like").put(feedId, count);
      }
    }

    return likeCountMap;
  }
}
