package com.example.ootd.domain.feed.service.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.ootd.domain.feed.repository.FeedLikeRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@ExtendWith(MockitoExtension.class)
public class FeedLikeCacheServiceTest {

  @Mock
  private CacheManager cacheManager;
  @Mock
  private FeedLikeRepository feedLikeRepository;
  @Mock
  private Cache cache;

  @InjectMocks
  private FeedLikeCacheService feedLikeCacheService;


  private UUID userId;
  private List<UUID> likedFeedIds;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    likedFeedIds = List.of(UUID.randomUUID(), UUID.randomUUID());
  }

  @Nested
  @DisplayName("getFeedLikeMapByUserId()")
  class GetFeedLikeMapByUserId {

    @Test
    @DisplayName("캐시 미스 시 DB 조회 후 Map 반환")
    void shouldReturnMapOfLikedFeedIds() {
      // given
      given(feedLikeRepository.findFeedIdsByUserId(userId)).willReturn(likedFeedIds);

      // when
      Map<String, Boolean> result = feedLikeCacheService.getFeedLikeMapByUserId(userId);

      // then
      assertThat(result).hasSize(2);
      assertThat(result).containsKeys(
          likedFeedIds.get(0).toString(),
          likedFeedIds.get(1).toString()
      );
      assertThat(result.values()).allMatch(val -> val.equals(true));
    }
  }

  @Nested
  @DisplayName("refreshFeedLikeMap()")
  class RefreshFeedLikeMap {

    @Test
    @DisplayName("캐시에 강제로 Map을 갱신한다")
    void shouldRefreshCache() {
      // given
      given(cacheManager.getCache("feed_like")).willReturn(cache);
      given(feedLikeRepository.findFeedIdsByUserId(userId)).willReturn(likedFeedIds);

      Map<String, Boolean> expectedMap = new HashMap<>();
      for (UUID id : likedFeedIds) {
        expectedMap.put(id.toString(), true);
      }

      // when
      feedLikeCacheService.refreshFeedLikeMap(userId);

      // then
      verify(cacheManager).getCache("feed_like");
      verify(feedLikeRepository).findFeedIdsByUserId(userId);
      verify(cache).put(userId, expectedMap);
    }

    @Test
    @DisplayName("캐시가 null이면 아무 것도 하지 않는다")
    void shouldDoNothingWhenCacheIsNull() {
      // given
      given(cacheManager.getCache("feed_like")).willReturn(null);

      // when
      feedLikeCacheService.refreshFeedLikeMap(userId);

      // then
      verify(feedLikeRepository, never()).findFeedIdsByUserId(any());
      verify(cache, never()).put(any(), any());
    }
  }
}
