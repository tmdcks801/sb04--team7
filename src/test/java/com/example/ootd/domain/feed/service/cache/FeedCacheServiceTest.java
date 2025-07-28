package com.example.ootd.domain.feed.service.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.ootd.domain.feed.dto.data.FeedCountDto;
import com.example.ootd.domain.feed.entity.Feed;
import com.example.ootd.domain.feed.repository.FeedRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
public class FeedCacheServiceTest {

  @Mock
  private FeedRepository feedRepository;
  @Mock
  private CacheManager cacheManager;
  @Mock
  private Cache cache;

  @InjectMocks
  private FeedCacheService feedCacheService;

  UUID feedId = UUID.randomUUID();

  @Nested
  @DisplayName("getFeedStates() - 피드의 FeedCountDto를 캐시에서 가져옴")
  class GetFeedStatesTest {

    @Test
    @DisplayName("캐시 없을 경우: repository에서 조회 후 캐시에 저장")
    void getFeedStates_singleCacheMiss_shouldCallRepositoryAndCachePut() {

      // given
      FeedCountDto feedCountDto = new FeedCountDto(feedId, 5, 10);
      given(cacheManager.getCache("feed")).willReturn(cache);
      given(cache.get(eq(feedId), eq(FeedCountDto.class))).willReturn(null);
      given(feedRepository.countLikesAndCommentsByFeedIds(List.of(feedId)))
          .willReturn(List.of(feedCountDto));

      // when
      Map<UUID, FeedCountDto> result = feedCacheService.getFeedStates(List.of(feedId));

      // then
      assertThat(result).containsEntry(feedId, feedCountDto);
      verify(cache).put(eq(feedId), eq(feedCountDto));
    }

    @Test
    @DisplayName("캐시에 있을 경우: repository를 호출하지 않고 캐시 값을 반환한다")
    void getFeedStates_singleCacheHit_shouldReturnCachedValueOnly() {
      // given
      FeedCountDto cachedDto = new FeedCountDto(feedId, 3, 7);
      given(cacheManager.getCache("feed")).willReturn(cache);
      given(cache.get(eq(feedId), eq(FeedCountDto.class))).willReturn(cachedDto);

      // when
      Map<UUID, FeedCountDto> result = feedCacheService.getFeedStates(List.of(feedId));

      // then
      assertThat(result).containsEntry(feedId, cachedDto);
      verify(feedRepository, never()).countLikesAndCommentsByFeedIds(anyList());
    }
  }
}
