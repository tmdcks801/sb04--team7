package com.example.ootd.domain.feed.service.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.ootd.TestEntityFactory;
import com.example.ootd.domain.feed.dto.data.CommentDto;
import com.example.ootd.domain.feed.dto.request.FeedCommentSearchCondition;
import com.example.ootd.domain.feed.entity.Feed;
import com.example.ootd.domain.feed.entity.FeedComment;
import com.example.ootd.domain.feed.mapper.CommentMapper;
import com.example.ootd.domain.feed.repository.FeedCommentRepository;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.dto.AuthorDto;
import com.example.ootd.domain.weather.entity.PrecipitationType;
import com.example.ootd.domain.weather.entity.SkyStatus;
import com.example.ootd.domain.weather.entity.Weather;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@ExtendWith(MockitoExtension.class)
public class FeedCommentCacheServiceTest {

  @Mock
  private FeedCommentRepository feedCommentRepository;
  @Mock
  private CommentMapper commentMapper;
  @Mock
  private CacheManager cacheManager;
  @Mock
  private Cache commentCache;
  @Mock
  private Cache keyCache;

  @InjectMocks
  private FeedCommentCacheService feedCommentCacheService;

  private UUID feedId;
  private FeedCommentSearchCondition condition;
  private List<FeedComment> mockEntities;
  private List<CommentDto> mockDtos;

  @BeforeEach
  void setUp() {
    feedId = UUID.randomUUID();
    condition = mock(FeedCommentSearchCondition.class);

    User user = TestEntityFactory.createUser();
    Weather weather = TestEntityFactory.createWeather(SkyStatus.CLOUDY, PrecipitationType.NONE);
    Feed feed = TestEntityFactory.createFeed(user, weather);
    mockEntities = List.of(new FeedComment(feed, user, "test content"));
    mockDtos = List.of(
        new CommentDto(UUID.randomUUID(), new AuthorDto(user.getId(), user.getName(), null),
            "test content", LocalDateTime.now(), feedId));
  }

  @Nested
  @DisplayName("getCachedComments() - 댓글 조회 결과 캐시 저장 및 조회")
  class GetCachedCommentsTest {

    @Test
    @DisplayName("댓글 캐시 MISS 발생 시 DB에서 조회하고 키 저장소에 키 등록")
    void getCachedComments_shouldQueryRepositoryAndSaveToCacheKeys() {

      // given
      LocalDateTime cursor = LocalDateTime.now();
      given(feedCommentRepository.findByCondition(condition, feedId)).willReturn(mockEntities);
      given(commentMapper.toDto(mockEntities)).willReturn(mockDtos);
      given(cacheManager.getCache("feed_comment_key")).willReturn(keyCache);
      given(condition.toSimpleKey()).willReturn(String.format(
          ":cursor=%s:idAfter=%s:limit=%s", cursor, feedId, 5));

      ArgumentCaptor<Set<String>> keyCaptor = ArgumentCaptor.forClass(Set.class);

      given(keyCache.get(feedId, Set.class)).willReturn(null);

      // when
      List<CommentDto> result = feedCommentCacheService.getCachedComments(feedId, condition);

      // then
      verify(feedCommentRepository).findByCondition(condition, feedId);
      verify(commentMapper).toDto(mockEntities);
      verify(keyCache).put(eq(feedId), keyCaptor.capture());

      Set<String> savedKeys = keyCaptor.getValue();
      assertThat(savedKeys).contains("feedId=" + feedId + String.format(
          ":cursor=%s:idAfter=%s:limit=%s", cursor, feedId, 5));
      assertThat(result).isEqualTo(mockDtos);
    }
  }

  @Nested
  @DisplayName("deleteAllCommentCacheByFeedId() - 해당 피드 관련 캐시 키 삭제")
  class DeleteAllCommentCacheByFeedId {

    @Test
    @DisplayName("해당 feedId의 모든 댓글 캐시 키들을 제거한다")
    void deleteAllCommentCacheByFeedId_shouldEvictAllRelevantKeys() {
      // given
      String key1 = "feedId=" + feedId + "|cursor=1|size=3";
      String key2 = "feedId=" + feedId + "|cursor=2|size=3";
      Set<String> keys = new HashSet<>(Set.of(key1, key2));

      given(cacheManager.getCache("feed_comment_key")).willReturn(keyCache);
      given(keyCache.get(feedId, Set.class)).willReturn(keys);
      given(cacheManager.getCache("feed_comment")).willReturn(commentCache);

      // when
      feedCommentCacheService.deleteAllCommentCacheByFeedId(feedId);

      // then
      verify(commentCache).evict(key1);
      verify(commentCache).evict(key2);
      verify(commentCache).evict(feedId);
      verify(keyCache).evict(feedId);
    }
  }
}