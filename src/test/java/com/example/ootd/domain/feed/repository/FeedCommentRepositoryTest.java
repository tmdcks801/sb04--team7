package com.example.ootd.domain.feed.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ootd.TestEntityFactory;
import com.example.ootd.domain.feed.dto.request.FeedCommentSearchCondition;
import com.example.ootd.domain.feed.entity.Feed;
import com.example.ootd.domain.feed.entity.FeedComment;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.domain.weather.entity.PrecipitationType;
import com.example.ootd.domain.weather.entity.SkyStatus;
import com.example.ootd.domain.weather.entity.Weather;
import com.example.ootd.domain.weather.repository.WeatherRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FeedCommentRepositoryTest {

  @Autowired
  private FeedRepository feedRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private WeatherRepository weatherRepository;
  @Autowired
  private FeedCommentRepository feedCommentRepository;

  private UUID feedId;
  private final List<FeedComment> commentList = new ArrayList<>();

  @BeforeEach
  void setUp() {
    User user1 = userRepository.save(TestEntityFactory.createUserWithoutId("1"));
    User user2 = userRepository.save(TestEntityFactory.createUserWithoutId("2"));
    Weather weather = weatherRepository.save(
        TestEntityFactory.createWeatherWithoutId(SkyStatus.CLEAR, PrecipitationType.NONE));
    Feed feed = feedRepository.save(
        Feed.builder().user(user1).weather(weather).content("comment test").build());
    feedId = feed.getId();
    for (int i = 0; i < 3; i++) {
      FeedComment comment = FeedComment.builder().feed(feed).user(user1)
          .content("user1 - comment test" + i).build();
      commentList.add(comment);
    }
    for (int i = 0; i < 3; i++) {
      FeedComment comment = FeedComment.builder().feed(feed).user(user2)
          .content("user2 - comment test" + i).build();
      commentList.add(comment);
    }
    feedCommentRepository.saveAll(commentList);
    commentList.sort(Comparator.comparing(FeedComment::getCreatedAt)
        .thenComparing(c -> c.getId().toString()));
  }

  @Nested
  @DisplayName("findByCondition() - 검색 조건에 맞는 댓글 검색")
  class FindByConditionTesT {

    @Test
    @DisplayName("성공 - 필수요건(limit, feedId) 입력 시")
    void findByCondition_success() {

      // given
      FeedCommentSearchCondition condition = FeedCommentSearchCondition.builder().limit(5).build();

      // when
      List<FeedComment> result = feedCommentRepository.findByCondition(condition, feedId);

      // then
      assertThat(result).hasSize(6);
      assertThat(result).containsExactlyElementsOf(commentList);
    }

//    @Test
//    @DisplayName("성공 - 커서 페이지네이션")
//    void findByCondition_success_cursor() {
//
//      // given
//      FeedCommentSearchCondition condition = FeedCommentSearchCondition.builder().limit(2)
//          .cursor(commentList.get(1).getCreatedAt()).idAfter(commentList.get(1).getId()).build();
//
//      // when
//      List<FeedComment> result = feedCommentRepository.findByCondition(condition, feedId);
//
//      // then
//      assertThat(result).hasSize(3);
//      assertThat(result).containsExactlyElementsOf(commentList.subList(2, 5));
//    }
  }

  @Nested
  @DisplayName("countByFeedId() - 해당 피드의 댓글 개수")
  class CountByFeedIdTest {

    @Test
    @DisplayName("성공 - 피드의 댓글 개수 조회 성공")
    void countByFeedId_success() {

      // when
      long count = feedCommentRepository.countByFeedId(feedId);

      // then
      assertThat(count).isEqualTo(6);
    }
  }
}
