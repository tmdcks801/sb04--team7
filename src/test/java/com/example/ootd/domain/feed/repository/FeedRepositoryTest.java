package com.example.ootd.domain.feed.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ootd.TestEntityFactory;
import com.example.ootd.domain.feed.dto.request.FeedSearchCondition;
import com.example.ootd.domain.feed.entity.Feed;
import com.example.ootd.domain.image.service.S3Service;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.domain.weather.entity.PrecipitationType;
import com.example.ootd.domain.weather.entity.SkyStatus;
import com.example.ootd.domain.weather.entity.Weather;
import com.example.ootd.domain.weather.repository.WeatherRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest // 의존이 너무 많아서 SpringBootTest 사용
@ActiveProfiles("test")
@Transactional
public class FeedRepositoryTest {

  @Autowired
  private FeedRepository feedRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private WeatherRepository weatherRepository;

  @MockitoBean
  private S3Service s3Service; // 외부 호출 막기

  private User user;
  private Weather weather;
  private final List<Feed> feedList = new ArrayList<>();

  @BeforeEach
  void setUp() {
    user = userRepository.save(TestEntityFactory.createUserWithoutId("test"));
    for (int i = 0; i < 3; i++) {
      weather = weatherRepository.save(TestEntityFactory.createWeatherWithoutId(SkyStatus.CLEAR,
          PrecipitationType.NONE));
      Feed feed = Feed.builder().user(user).weather(weather).content("test " + i).build();
      if (i == 0) {
        feed.increaseLikeCount();
      }
      feedList.add(feed);
    }
    for (int i = 0; i < 3; i++) {
      weather = weatherRepository.save(
          TestEntityFactory.createWeatherWithoutId(SkyStatus.MOSTLY_CLOUDY,
              PrecipitationType.RAIN));
      Feed feed = Feed.builder().user(user).weather(weather).content("test rain " + i).build();
      if (i == 1) {
        feed.increaseLikeCount();
      }
      feedList.add(feed);
    }
    feedRepository.saveAll(feedList);
  }

  @Nested
  @DisplayName("findByCondition() - 검색 조건에 맞는 피드 검색")
  class FindByConditionTest {

    @Test
    @DisplayName("성공 - 필수요건만 있는 경우, 최신순")
    void findByCondition_success_createdAt_desc() {

      // given
      FeedSearchCondition condition = FeedSearchCondition.builder().limit(3).sortBy("createdAt")
          .sortDirection("DESCENDING").build();
      feedList.sort(Comparator.comparing(Feed::getCreatedAt, Comparator.reverseOrder())
          .thenComparing(f -> f.getId().toString(), Comparator.reverseOrder()));

      // when
      List<Feed> result = feedRepository.findByCondition(condition);

      // then
      assertThat(result).hasSize(4);
      assertThat(result).containsExactlyElementsOf(feedList.subList(0, 4));
    }

    @Test
    @DisplayName("성공 - 필수요건만 있는 경우, 오래된 순")
    void findByCondition_success_createdAt_asc() {

      // given
      FeedSearchCondition condition = FeedSearchCondition.builder().limit(3).sortBy("createdAt")
          .sortDirection("ASCENDING").build();
      feedList.sort(Comparator.comparing(Feed::getCreatedAt)
          .thenComparing(f -> f.getId().toString()));

      // when
      List<Feed> result = feedRepository.findByCondition(condition);

      // then
      assertThat(result).hasSize(4);
      assertThat(result).containsExactlyElementsOf(feedList.subList(0, 4));
    }

    @Test
    @DisplayName("성공 - 필수요건만 있는 경우, 좋아요 많은 순")
    void findByCondition_success_likeCount_desc() {

      // given
      FeedSearchCondition condition = FeedSearchCondition.builder().limit(3).sortBy("likeCount")
          .sortDirection("DESCENDING").build();
      feedList.sort(Comparator.comparing(Feed::getLikeCount, Comparator.reverseOrder())
          .thenComparing(f -> f.getId().toString(), Comparator.reverseOrder()));

      // when
      List<Feed> result = feedRepository.findByCondition(condition);

      // then
      assertThat(result).hasSize(4);
      assertThat(result).containsExactlyElementsOf(feedList.subList(0, 4));
    }

    @Test
    @DisplayName("성공 - 필수요건만 있는 경우, 좋아요 적은 순")
    void findByCondition_success_likeCount_asc() {

      // given
      FeedSearchCondition condition = FeedSearchCondition.builder().limit(3).sortBy("likeCount")
          .sortDirection("ASCENDING").build();
      feedList.sort(Comparator.comparing(Feed::getLikeCount)
          .thenComparing(f -> f.getId().toString()));

      // when
      List<Feed> result = feedRepository.findByCondition(condition);

      // then
      assertThat(result).hasSize(4);
      assertThat(result).containsExactlyElementsOf(feedList.subList(0, 4));
    }

//    @Test
//    @DisplayName("성공 - 최신순, 커서 페이지네이션")
//    void findByCondition_success_createdAt_desc_cursor() {
//
//      // given
//      feedList.sort(Comparator.comparing(Feed::getCreatedAt, Comparator.reverseOrder())
//          .thenComparing(f -> f.getId().toString(), Comparator.reverseOrder()));
//      Feed cursor = feedList.get(1);
//      FeedSearchCondition condition = FeedSearchCondition.builder().limit(2).sortBy("createdAt")
//          .sortDirection("DESCENDING").cursor(cursor.getCreatedAt().toString())
//          .idAfter(cursor.getId()).build();
//
//      // when
//      List<Feed> result = feedRepository.findByCondition(condition);
//
//      // then
//      assertThat(result).hasSize(3);
//      assertThat(result).containsExactlyElementsOf(feedList.subList(2, 5));
//    }
//
//    @Test
//    @DisplayName("성공 - 오래된 순, 커서 페이지네이션")
//    void findByCondition_success_createdAt_asc_cursor() {
//
//      // given
//      feedList.sort(Comparator.comparing(Feed::getCreatedAt)
//          .thenComparing(f -> f.getId().toString()));
//      Feed cursor = feedList.get(1);
//      FeedSearchCondition condition = FeedSearchCondition.builder().limit(2).sortBy("createdAt")
//          .sortDirection("ASCENDING").cursor(cursor.getCreatedAt().toString())
//          .idAfter(cursor.getId()).build();
//
//      // when
//      List<Feed> result = feedRepository.findByCondition(condition);
//
//      // then
//      assertThat(result).hasSize(3);
//      assertThat(result).containsExactlyElementsOf(feedList.subList(2, 5));
//    }

    @Test
    @DisplayName("성공 - 좋아요 많은 순, 커서 페이지네이션")
    void findByCondition_success_likeCount_desc_cursor() {

      // given
      feedList.sort(Comparator.comparing(Feed::getLikeCount, Comparator.reverseOrder())
          .thenComparing(f -> f.getId().toString(), Comparator.reverseOrder()));
      Feed cursor = feedList.get(1);
      FeedSearchCondition condition = FeedSearchCondition.builder().limit(2).sortBy("likeCount")
          .sortDirection("DESCENDING").cursor(String.valueOf(cursor.getLikeCount()))
          .idAfter(cursor.getId()).build();

      // when
      List<Feed> result = feedRepository.findByCondition(condition);

      // then
      assertThat(result).hasSize(3);
      assertThat(result).containsExactlyElementsOf(feedList.subList(2, 5));
    }

    @Test
    @DisplayName("성공 - 좋아요 적은 순, 커서 페이지네이션")
    void findByCondition_success_likeCount_asc_cursor() {

      // given
      feedList.sort(Comparator.comparing(Feed::getLikeCount)
          .thenComparing(f -> f.getId().toString()));
      Feed cursor = feedList.get(1);
      FeedSearchCondition condition = FeedSearchCondition.builder().limit(2).sortBy("likeCount")
          .sortDirection("ASCENDING").cursor(String.valueOf(cursor.getLikeCount()))
          .idAfter(cursor.getId()).build();

      // when
      List<Feed> result = feedRepository.findByCondition(condition);

      // then
      assertThat(result).hasSize(3);
      assertThat(result).containsExactlyElementsOf(feedList.subList(2, 5));
    }

    @Test
    @DisplayName("성공 - 검색어 입력")
    void findByCondition_success_keyword() {

      // given
      FeedSearchCondition condition = FeedSearchCondition.builder().limit(10).sortBy("createdAt")
          .sortDirection("DESCENDING").keywordLike("test rain").build();

      // when
      List<Feed> result = feedRepository.findByCondition(condition);

      // then
      assertThat(result).hasSize(3);
      assertThat(result).contains(feedList.get(3), feedList.get(4), feedList.get(5));
    }

    @Test
    @DisplayName("성공 - 날씨 입력")
    void findByCondition_success_sky() {

      // given
      FeedSearchCondition condition = FeedSearchCondition.builder().limit(10).sortBy("createdAt")
          .sortDirection("DESCENDING").skyStatusEqual(SkyStatus.MOSTLY_CLOUDY).build();

      // when
      List<Feed> result = feedRepository.findByCondition(condition);

      // then
      assertThat(result).hasSize(3);
      assertThat(result).contains(feedList.get(3), feedList.get(4), feedList.get(5));
    }

    @Test
    @DisplayName("성공 - 강수 입력")
    void findByCondition_success_precipitation() {

      // given
      FeedSearchCondition condition = FeedSearchCondition.builder().limit(10).sortBy("createdAt")
          .sortDirection("DESCENDING").precipitationTypeEqual(PrecipitationType.NONE).build();

      // when
      List<Feed> result = feedRepository.findByCondition(condition);

      // then
      assertThat(result).hasSize(3);
      assertThat(result).contains(feedList.get(0), feedList.get(1), feedList.get(2));
    }

    @Test
    @DisplayName("성공 - 작성자 입력")
    void findByCondition_success_authorId() {

      // given
      FeedSearchCondition condition = FeedSearchCondition.builder().limit(10).sortBy("createdAt")
          .sortDirection("DESCENDING").authorIdEqual(user.getId()).build();

      // when
      List<Feed> result = feedRepository.findByCondition(condition);

      // then
      assertThat(result).hasSize(6);
      assertThat(result).containsAll(feedList);
    }
  }

  @Nested
  @DisplayName("countByCondition() - 검색 조건에 맞는 옷 개수")
  class CountByConditionTest {

    @Test
    @DisplayName("성공 - 작성자 id 작성한 경우")
    void countByCondition_success() {

      // given
      FeedSearchCondition condition = FeedSearchCondition.builder().limit(100).sortBy("createdAt")
          .sortDirection("DESCENDING").authorIdEqual(user.getId()).build();

      // when
      long count = feedRepository.countByCondition(condition);

      // then
      assertThat(count).isEqualTo(6L);
    }

    @Test
    @DisplayName("성공 - 키워드 작성한 경우")
    void countByCondition_success_keyword() {

      // given
      FeedSearchCondition condition = FeedSearchCondition.builder().limit(100).sortBy("createdAt")
          .sortDirection("DESCENDING").keywordLike("test rain").build();

      // when
      long count = feedRepository.countByCondition(condition);

      // then
      assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("성공 - 날씨 작성한 경우")
    void countByCondition_success_sky() {

      // given
      FeedSearchCondition condition = FeedSearchCondition.builder().limit(100).sortBy("createdAt")
          .sortDirection("DESCENDING").skyStatusEqual(SkyStatus.CLEAR)
          .build();

      // when
      long count = feedRepository.countByCondition(condition);

      // then
      assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("성공 - 강수 작성한 경우")
    void countByCondition_success_precipitation() {

      // given
      FeedSearchCondition condition = FeedSearchCondition.builder().limit(100).sortBy("createdAt")
          .sortDirection("DESCENDING")
          .precipitationTypeEqual(PrecipitationType.RAIN)
          .build();

      // when
      long count = feedRepository.countByCondition(condition);

      // then
      assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("성공 - 결과가 null일 때 0 반환")
    void countByCondition_success_null() {

      // given
      FeedSearchCondition condition = FeedSearchCondition.builder().limit(100).sortBy("createdAt")
          .sortDirection("DESCENDING").authorIdEqual(user.getId())
          .precipitationTypeEqual(PrecipitationType.SHOWER)
          .build();

      // when
      long count = feedRepository.countByCondition(condition);

      // then
      assertThat(count).isEqualTo(0L);
    }
  }
}
