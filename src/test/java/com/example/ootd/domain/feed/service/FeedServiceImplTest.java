package com.example.ootd.domain.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.ootd.TestEntityFactory;
import com.example.ootd.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.example.ootd.domain.clothes.dto.data.OotdDto;
import com.example.ootd.domain.clothes.entity.Clothes;
import com.example.ootd.domain.clothes.repository.ClothesRepository;
import com.example.ootd.domain.feed.dto.data.CommentDto;
import com.example.ootd.domain.feed.dto.data.FeedDto;
import com.example.ootd.domain.feed.dto.request.CommentCreateRequest;
import com.example.ootd.domain.feed.dto.request.FeedCommentSearchCondition;
import com.example.ootd.domain.feed.dto.request.FeedCreateRequest;
import com.example.ootd.domain.feed.dto.request.FeedSearchCondition;
import com.example.ootd.domain.feed.dto.request.FeedUpdateRequest;
import com.example.ootd.domain.feed.entity.Feed;
import com.example.ootd.domain.feed.entity.FeedComment;
import com.example.ootd.domain.feed.entity.FeedLike;
import com.example.ootd.domain.feed.mapper.CommentMapper;
import com.example.ootd.domain.feed.mapper.FeedMapper;
import com.example.ootd.domain.feed.repository.FeedCommentRepository;
import com.example.ootd.domain.feed.repository.FeedLikeRepository;
import com.example.ootd.domain.feed.repository.FeedRepository;
import com.example.ootd.domain.feed.service.impl.FeedServiceImpl;
import com.example.ootd.domain.follow.repository.FollowRepository;
import com.example.ootd.domain.notification.service.inter.NotificationPublisherInterface;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.dto.AuthorDto;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.domain.weather.dto.WeatherSummaryDto;
import com.example.ootd.domain.weather.entity.Weather;
import com.example.ootd.domain.weather.repository.WeatherRepository;
import com.example.ootd.dto.PageResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class FeedServiceImplTest {

  @Mock
  private FeedRepository feedRepository;
  @Mock
  private FeedLikeRepository feedLikeRepository;
  @Mock
  private FeedCommentRepository feedCommentRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private WeatherRepository weatherRepository;
  @Mock
  private ClothesRepository clothesRepository;
  @Mock
  private FollowRepository followRepository;
  @Mock
  private FeedMapper feedMapper;
  @Mock
  private CommentMapper commentMapper;
  @Mock
  private NotificationPublisherInterface notificationPublisher;

  @InjectMocks
  private FeedServiceImpl feedService;

  private UUID userId;
  private UUID feedId;
  private UUID weatherId;

  @BeforeEach
  void setup() {
    userId = UUID.randomUUID();
    feedId = UUID.randomUUID();
    weatherId = UUID.randomUUID();
  }

  @Nested
  @DisplayName("createFeed() - 피드 등록 테스트")
  class CreateFeedTest {

    @Test
    @DisplayName("성공 - 등록 성공")
    void createFeed_success() {
      // given
      UUID authorId = UUID.randomUUID();
      UUID weatherId = UUID.randomUUID();
      List<UUID> clothesIds = List.of(UUID.randomUUID());

      FeedCreateRequest request = new FeedCreateRequest(
          authorId, weatherId, clothesIds, "테스트"
      );

      User author = User.builder()
          .name("테스트 사용자")
          .build();

      Weather weather = Weather.builder()
          .id(weatherId)
          .build();

      Clothes clothes = Clothes.builder()
          .name("반팔 티셔츠")
          .build();

      Feed savedFeed = Feed.builder()
          .user(author)
          .weather(weather)
          .content(request.content())
          .build();

      given(userRepository.findById(authorId)).willReturn(Optional.of(author));
      given(weatherRepository.findById(weatherId)).willReturn(Optional.of(weather));
      given(clothesRepository.findAllById(clothesIds)).willReturn(List.of(clothes));
      given(feedRepository.save(any())).willReturn(savedFeed);
      given(followRepository.findFollowersByFolloweeId(authorId)).willReturn(
          List.of(UUID.randomUUID()));
      given(feedMapper.toDto(any(Feed.class), eq(false))).willAnswer(invocation -> {
        Feed feed = invocation.getArgument(0);
        UUID feedId = UUID.randomUUID(); // 새로 ID 하나 생성
        return FeedDto.builder()
            .id(feedId)
            .author(new AuthorDto(authorId, author.getName(), "https://example.com/profile.jpg"))
            .weather(new WeatherSummaryDto(weatherId, null, null, null))
            .ootds(List.of(OotdDto.builder()
                .clothesId(UUID.randomUUID())
                .name("반팔 티셔츠")
                .imageUrl("https://example.com/clothes.jpg")
                .type(null)
                .attributes(List.of(ClothesAttributeWithDefDto.builder()
                    .definitionId(UUID.randomUUID())
                    .definitionName("색상")
                    .selectableValues(List.of("빨강", "파랑"))
                    .value("파랑")
                    .build()))
                .build()))
            .content(feed.getContent())
            .likeCount(0L)
            .commentCount(0)
            .likedByMe(false)
            .build();
      });

      // when
      FeedDto result = feedService.createFeed(request);

      // then
      assertThat(result).isNotNull();
      assertThat(result.id()).isNotNull();
      assertThat(result.author().name()).isEqualTo("테스트 사용자");
      assertThat(result.weather().weatherId()).isEqualTo(weatherId);
      assertThat(result.ootds()).hasSize(1);
      assertThat(result.ootds().get(0).name()).isEqualTo("반팔 티셔츠");
      assertThat(result.content()).isEqualTo("테스트");

      verify(feedRepository).save(any(Feed.class));
      verify(notificationPublisher).publishToMany(any(), any());
    }
  }

  @Nested
  @DisplayName("updateFeed() - 피드 수정 테스트")
  class UpdateFeedTest {

    @Test
    @DisplayName("성공 - 수정 성공")
    void updateFeed_success() {
      // given
      Feed feed = Feed.builder().content("old content").build();
      given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
      given(feedMapper.toDto(any(), anyBoolean())).willReturn(
          FeedDto.builder().id(feedId).content("new content").build());

      // when
      FeedDto result = feedService.updateFeed(feedId, new FeedUpdateRequest("new content"), userId);

      // then
      assertThat(result).isNotNull();
      assertThat(result.content()).isEqualTo("new content");
    }
  }

  @Nested
  @DisplayName("findFeedByCondition() - 피드 목록 조회 테스트")
  class FindFeedByConditionTest {

    @Test
    @DisplayName("성공 - 목록 조회 성공, 다음 페이지 없는 경우")
    void findFeedByCondition_success() {
      // given
      FeedSearchCondition condition = FeedSearchCondition.builder()
          .limit(10)
          .sortBy("createdAt")
          .sortDirection("ASCENDING")
          .build();

      User author = TestEntityFactory.createUser();

      Weather weather = Weather.builder()
          .id(weatherId)
          .build();

      Feed feed = Feed.builder()
          .user(author)
          .weather(weather)
          .content("테스트 피드 내용")
          .build();
      ReflectionTestUtils.setField(feed, "id", feedId);

      // 좋아요 맵
      FeedLike feedLike = new FeedLike(feed, author);

      given(feedRepository.findByCondition(condition)).willReturn(List.of(feed));
      given(feedLikeRepository.findAllByUserId(userId)).willReturn(List.of(feedLike));
      given(feedMapper.toDto(eq(List.of(feed)), anyMap())).willReturn(List.of(
          FeedDto.builder()
              .id(feedId)
              .author(new AuthorDto(userId, author.getName(), "https://example.com/profile.jpg"))
              .weather(new WeatherSummaryDto(weatherId, null, null, null))
              .ootds(List.of(
                  OotdDto.builder()
                      .clothesId(UUID.randomUUID())
                      .name("청바지")
                      .imageUrl("https://example.com/clothes.jpg")
                      .type(null)
                      .attributes(List.of(
                          ClothesAttributeWithDefDto.builder()
                              .definitionId(UUID.randomUUID())
                              .definitionName("색상")
                              .selectableValues(List.of("검정", "청색"))
                              .value("청색")
                              .build()
                      ))
                      .build()
              ))
              .content("테스트 피드 내용")
              .likeCount(1L)
              .commentCount(2)
              .likedByMe(true)
              .build()
      ));
      given(feedRepository.countByCondition(condition)).willReturn(1L);

      // when
      PageResponse<FeedDto> result = feedService.findFeedByCondition(condition, userId);

      // then
      assertThat(result).isNotNull();
      assertThat(result.data()).hasSize(1);
      assertThat(result.hasNext()).isFalse();
      assertThat(result.totalCount()).isEqualTo(1);
      assertThat(result.data().get(0).content()).isEqualTo("테스트 피드 내용");
      assertThat(result.data().get(0).author().name()).isEqualTo("test-name");

      verify(feedRepository).findByCondition(condition);
      verify(feedLikeRepository).findAllByUserId(userId);
      verify(feedRepository).countByCondition(condition);
    }

    @Test
    @DisplayName("성공 - 목록 조회 성공, 다음 페이지 있는 경우")
    void findFeedByCondition_success_has_next_page() {
      // given
      FeedSearchCondition condition = FeedSearchCondition.builder()
          .limit(1)
          .sortBy("createdAt")
          .sortDirection("ASCENDING")
          .build();

      User author = TestEntityFactory.createUser();

      Weather weather = Weather.builder()
          .id(weatherId)
          .build();

      Feed feed1 = Feed.builder()
          .user(author)
          .weather(weather)
          .content("테스트 피드 내용1")
          .build();
      ReflectionTestUtils.setField(feed1, "id", feedId);
      ReflectionTestUtils.setField(feed1, "createdAt", LocalDateTime.now());
      Feed feed2 = Feed.builder()
          .user(author)
          .weather(weather)
          .content("테스트 피드 내용2")
          .build();
      ReflectionTestUtils.setField(feed2, "id", feedId);
      ReflectionTestUtils.setField(feed2, "createdAt", LocalDateTime.now());

      // 좋아요 맵
      FeedLike feedLike = new FeedLike(feed1, author);

      given(feedRepository.findByCondition(condition)).willReturn(
          new ArrayList<>(List.of(feed1, feed2)));
      given(feedLikeRepository.findAllByUserId(userId)).willReturn(List.of(feedLike));
      given(feedMapper.toDto(eq(List.of(feed1)), anyMap())).willReturn(List.of(
          FeedDto.builder()
              .id(feedId)
              .author(new AuthorDto(userId, author.getName(), "https://example.com/profile.jpg"))
              .weather(new WeatherSummaryDto(weatherId, null, null, null))
              .ootds(List.of(
                  OotdDto.builder()
                      .clothesId(UUID.randomUUID())
                      .name("청바지")
                      .imageUrl("https://example.com/clothes.jpg")
                      .type(null)
                      .attributes(List.of(
                          ClothesAttributeWithDefDto.builder()
                              .definitionId(UUID.randomUUID())
                              .definitionName("색상")
                              .selectableValues(List.of("검정", "청색"))
                              .value("청색")
                              .build()
                      ))
                      .build()
              ))
              .content("테스트 피드 내용")
              .likeCount(1L)
              .commentCount(2)
              .likedByMe(true)
              .build()
      ));
      given(feedRepository.countByCondition(condition)).willReturn(2L);

      // when
      PageResponse<FeedDto> result = feedService.findFeedByCondition(condition, userId);

      // then
      assertThat(result).isNotNull();
      assertThat(result.data()).hasSize(1);
      assertThat(result.hasNext()).isTrue();
      assertThat(result.totalCount()).isEqualTo(2);
      assertThat(result.data().get(0).content()).isEqualTo("테스트 피드 내용");
      assertThat(result.data().get(0).author().name()).isEqualTo("test-name");

      verify(feedRepository).findByCondition(condition);
      verify(feedLikeRepository).findAllByUserId(userId);
      verify(feedRepository).countByCondition(condition);
    }
  }

  @Nested
  @DisplayName("deleteFeed() - 피드 삭제 테스트")
  class DeleteFeedTest {

    @Test
    @DisplayName("성공 - 삭제 성공")
    void deleteFeed_success() {
      // given
      Feed feed = Feed.builder().build();
      given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));

      // when
      feedService.deleteFeed(feedId);

      // then
      verify(feedRepository).delete(feed);
    }
  }

  @Nested
  @DisplayName("likeFeed() - 피드 좋아요 테스트")
  class LikeFeedTest {

    @Test
    @DisplayName("성공 - 좋아요 성공")
    void likeFeed_success() {
      // given
      Feed feed = Feed.builder().user(TestEntityFactory.createUser()).build();
      User user = User.builder().name("user").build();
      given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(feedMapper.toDto(any(), eq(true))).willReturn(
          FeedDto.builder().likedByMe(true).build());

      // when
      FeedDto result = feedService.likeFeed(feedId, userId);

      // then
      assertThat(result).isNotNull();
      assertThat(result.likedByMe()).isTrue();
    }
  }

  @Nested
  @DisplayName("deleteFeedLike() - 피드 좋아요 삭제 테스트")
  class DeleteFeedLikeTest {

    @Test
    @DisplayName("성공 - 좋아요 삭제 성공")
    void deleteFeedLike_success() {
      Feed feed = Feed.builder().build();
      FeedLike like = new FeedLike(feed, TestEntityFactory.createUser());

      given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
      given(feedLikeRepository.findByFeedIdAndUserId(feedId, userId)).willReturn(Optional.of(like));

      // when
      feedService.deleteFeedLike(feedId, userId);

      // then
      verify(feedLikeRepository).delete(like);
    }
  }

  @Nested
  @DisplayName("createComment() - 피드 댓글 등록 테스트")
  class CreateCommentTest {

    @Test
    @DisplayName("성공 - 댓글 등록 성공")
    void createComment_success() {

      // given
      Feed feed = Feed.builder().user(TestEntityFactory.createUser()).build();
      User user = User.builder().name("댓글 사용자").build();
      FeedComment comment = FeedComment.builder().feed(feed).user(user).content("댓글입니다").build();

      given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(commentMapper.toDto(any(FeedComment.class))).willReturn(
          CommentDto.builder().id(UUID.randomUUID()).content("댓글입니다").build());

      // when
      CommentDto result = feedService.createComment(
          new CommentCreateRequest(feedId, userId, "댓글입니다."), userId);

      // then
      assertThat(result).isNotNull();
      assertThat(result.content()).isEqualTo("댓글입니다");
    }
  }

//  @Nested
//  @DisplayName("findCommentByCondition() - 피드 댓글 목록 조회 테스트")
//  class FindCommentByConditionTest {
//
//    @Test
//    @DisplayName("성공 - 댓글 목록 조회 성공, 다음 페이지 없는 경우")
//    void findCommentByCondition_success() {
//      // given
//      UUID feedId = UUID.randomUUID();
//      UUID userId = UUID.randomUUID();
//
//      FeedCommentSearchCondition condition = FeedCommentSearchCondition.builder()
//          .limit(10)
//          .build();
//
//      FeedComment comment = FeedComment.builder()
//          .content("테스트 댓글")
//          .user(TestEntityFactory.createUser())
//          .build();
//
//      List<FeedComment> commentList = List.of(comment);
//
//      given(feedCommentRepository.findByCondition(condition, feedId)).willReturn(commentList);
//      given(commentMapper.toDto(commentList)).willReturn(List.of(
//          CommentDto.builder()
//              .id(comment.getId())
//              .author(new AuthorDto(userId, "댓글 작성자", "https://example.com/profile.jpg"))
//              .content("테스트 댓글")
//              .createdAt(comment.getCreatedAt())
//              .build()
//      ));
//      given(feedCommentRepository.countByFeedId(feedId)).willReturn(1L);
//
//      // when
//      PageResponse<CommentDto> result = feedService.findCommentByCondition(feedId, condition);
//
//      // then
//      assertThat(result).isNotNull();
//      assertThat(result.data()).hasSize(1);
//      assertThat(result.hasNext()).isFalse();
//      assertThat(result.totalCount()).isEqualTo(1L);
//      assertThat(result.data().get(0).content()).isEqualTo("테스트 댓글");
//      assertThat(result.data().get(0).author().name()).isEqualTo("댓글 작성자");
//
//      verify(feedCommentRepository).findByCondition(condition, feedId);
//      verify(feedCommentRepository).countByFeedId(feedId);
//      verify(commentMapper).toDto(commentList);
//    }
//
//    @Test
//    @DisplayName("성공 - 댓글 목록 조회 성공, 다음 페이지 있는 경우")
//    void findCommentByCondition_success_has_next_page() {
//      // given
//      UUID feedId = UUID.randomUUID();
//      UUID userId = UUID.randomUUID();
//
//      FeedCommentSearchCondition condition = FeedCommentSearchCondition.builder()
//          .limit(1)
//          .build();
//
//      FeedComment comment1 = FeedComment.builder()
//          .content("테스트 댓글1")
//          .user(TestEntityFactory.createUser())
//          .build();
//      ReflectionTestUtils.setField(comment1, "createdAt", LocalDateTime.now());
//      FeedComment comment2 = FeedComment.builder()
//          .content("테스트 댓글2")
//          .user(TestEntityFactory.createUser())
//          .build();
//
//      List<FeedComment> commentList = new ArrayList<>(List.of(comment1, comment2));
//
//      given(feedCommentRepository.findByCondition(condition, feedId)).willReturn(commentList);
//      given(commentMapper.toDto(commentList)).willReturn(List.of(
//          CommentDto.builder()
//              .id(comment1.getId())
//              .author(new AuthorDto(userId, "댓글 작성자", "https://example.com/profile.jpg"))
//              .content("테스트 댓글1")
//              .createdAt(comment1.getCreatedAt())
//              .build()
//      ));
//      given(feedCommentRepository.countByFeedId(feedId)).willReturn(2L);
//
//      // when
//      PageResponse<CommentDto> result = feedService.findCommentByCondition(feedId, condition);
//
//      // then
//      assertThat(result).isNotNull();
//      assertThat(result.data()).hasSize(1);
//      assertThat(result.hasNext()).isTrue();
//      assertThat(result.totalCount()).isEqualTo(2L);
//      assertThat(result.data().get(0).content()).isEqualTo("테스트 댓글1");
//      assertThat(result.data().get(0).author().name()).isEqualTo("댓글 작성자");
//
//      verify(feedCommentRepository).findByCondition(condition, feedId);
//      verify(feedCommentRepository).countByFeedId(feedId);
//      verify(commentMapper).toDto(commentList);
//    }
//  }
}
