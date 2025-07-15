package com.example.ootd.domain.feed.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ootd.TestEntityFactory;
import com.example.ootd.domain.feed.entity.Feed;
import com.example.ootd.domain.feed.entity.FeedLike;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.domain.weather.entity.PrecipitationType;
import com.example.ootd.domain.weather.entity.SkyStatus;
import com.example.ootd.domain.weather.entity.Weather;
import com.example.ootd.domain.weather.repository.WeatherRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
public class FeedLikeRepositoryTest {

  @Autowired
  private FeedRepository feedRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private WeatherRepository weatherRepository;
  @Autowired
  private FeedLikeRepository feedLikeRepository;

  private User user1;
  private User user2;
  private User user3;
  private UUID feedId;
  private final List<FeedLike> likeList = new ArrayList<>();

  @BeforeEach
  void setUp() {
    user1 = userRepository.save(TestEntityFactory.createUserWithoutId("1"));
    user2 = userRepository.save(TestEntityFactory.createUserWithoutId("2"));
    user3 = userRepository.save(TestEntityFactory.createUserWithoutId("3"));
    Weather weather = weatherRepository.save(
        TestEntityFactory.createWeatherWithoutId(SkyStatus.CLEAR, PrecipitationType.NONE));
    Feed feed = feedRepository.save(
        Feed.builder().user(user1).weather(weather).content("comment test").build());
    feedId = feed.getId();

    FeedLike feedLike1 = new FeedLike(feed, user1);
    FeedLike feedLike2 = new FeedLike(feed, user2);
    likeList.add(feedLike1);
    likeList.add(feedLike2);
    feedLikeRepository.saveAll(likeList);
  }

  @Nested
  @DisplayName("findByFeedIdAndUserId() - 해당 피드에 대한 유저의 좋아요 정보 반환")
  class FindByFeedIdAndUserIdTest {

    @Test
    @DisplayName("성공 - 정보 있는 경우")
    void findByFeedIdAndUserId_success_exist() {

      // when
      Optional<FeedLike> feedLike = feedLikeRepository.findByFeedIdAndUserId(feedId, user2.getId());

      // then
      assertThat(feedLike).isNotEmpty();
      assertThat(feedLike.get().getUser()).isEqualTo(user2);
      assertThat(feedLike.get().getFeed().getId()).isEqualTo(feedId);
    }

    @Test
    @DisplayName("성공 - 정보 없는 경우")
    void findByFeedIdAndUserId_success_not_exist() {

      // when
      Optional<FeedLike> feedLike = feedLikeRepository.findByFeedIdAndUserId(feedId, user3.getId());

      // then
      assertThat(feedLike).isEmpty();
    }
  }

  @Nested
  @DisplayName("findAllByUserId() - 해당 유저의 모든 좋아요 정보 조회")
  class FindAllByUserIdTest {

    @Test
    @DisplayName("성공 - 조회 성공")
    void findAllByUserId_success() {

      // when
      List<FeedLike> result = feedLikeRepository.findAllByUserId(user2.getId());

      // then
      assertThat(result).hasSize(1);
      assertThat(result.get(0)).isEqualTo(likeList.get(1));
    }
  }
}
