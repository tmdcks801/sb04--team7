package com.example.ootd.domain.recommend.service;

import com.example.ootd.domain.clothes.entity.ClothesType;
import com.example.ootd.domain.recommend.dto.ScoredClothesDto;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("옷 점수 계산 테스트")
class ClothesCalculatorTest {

    @InjectMocks
    private ClothesCalculator clothesCalculator;

    @Test
    @DisplayName("점수 계산 테스트")
    void calculateScore() {
        // given
        ScoredClothesDto clothes = ScoredClothesDto.builder()
                .id(UUID.randomUUID())
                .ownerId(UUID.randomUUID())
                .name("Test Clothes")
                .type(ClothesType.TOP)
                .imageUrl("imageUrl")
                .thickness("얇음")
                .color("화이트")
                .season("여름")
                .temperatureCurrent(30.0)
                .windSpeed(1.0)
                .humidityCurrent(50.0)
                .precipitationAmount(0.0)
                .temperatureSensitivity(0)
                .build();

        // when
        double score = clothesCalculator.calculateScore(clothes);

        // then
        assertThat(score).isNotZero();
    }

    @Test
    @DisplayName("더운 날씨 점수 계산 테스트")
    void calculateScore_HotWeather() {
        // given
        ScoredClothesDto clothes = ScoredClothesDto.builder()
                .id(UUID.randomUUID())
                .ownerId(UUID.randomUUID())
                .name("Hot Weather Clothes")
                .type(ClothesType.TOP)
                .imageUrl("imageUrl")
                .thickness("얇음")
                .color("화이트")
                .season("여름")
                .temperatureCurrent(35.0)
                .windSpeed(0.5)
                .humidityCurrent(60.0)
                .precipitationAmount(0.0)
                .temperatureSensitivity(0)
                .build();

        // when
        double score = clothesCalculator.calculateScore(clothes);

        // then
        assertThat(score).isGreaterThan(50.0); // 점수가 높을 것으로 예상
    }

  @Test
  @DisplayName("따뜻한 날씨 점수 계산 테스트")
  void calculateScore_WarmWeather() {
    // given
    ScoredClothesDto clothes = ScoredClothesDto.builder()
        .id(UUID.randomUUID())
        .ownerId(UUID.randomUUID())
        .name("Warm Weather Clothes")
        .type(ClothesType.TOP)
        .imageUrl("imageUrl")
        .thickness("약간 얇음")
        .color("화이트")
        .season("봄")
        .temperatureCurrent(20.0)
        .windSpeed(1.0)
        .humidityCurrent(40.0)
        .precipitationAmount(0.0)
        .temperatureSensitivity(0)
        .build();

    // when
    double score = clothesCalculator.calculateScore(clothes);

    // then
    assertThat(score).isGreaterThan(50.0); // 점수가 높을 것으로 예상
  }

  @Test
  @DisplayName("시원한 날씨 점수 계산 테스트")
  void calculateScore_CoolWeather() {
    // given
    ScoredClothesDto clothes = ScoredClothesDto.builder()
        .id(UUID.randomUUID())
        .ownerId(UUID.randomUUID())
        .name("Cool Weather Clothes")
        .type(ClothesType.TOP)
        .imageUrl("imageUrl")
        .thickness("약간 두꺼움")
        .color("블랙")
        .season("가을")
        .temperatureCurrent(10.0)
        .windSpeed(1.0)
        .humidityCurrent(40.0)
        .precipitationAmount(0.0)
        .temperatureSensitivity(0)
        .build();

    // when
    double score = clothesCalculator.calculateScore(clothes);

    // then
    assertThat(score).isGreaterThan(50.0); // 점수가 높을 것으로 예상
  }

    @Test
    @DisplayName("추운 날씨 점수 계산 테스트")
    void calculateScore_ColdWeather() {
        // given
        ScoredClothesDto clothes = ScoredClothesDto.builder()
                .id(UUID.randomUUID())
                .ownerId(UUID.randomUUID())
                .name("Cold Weather Clothes")
                .type(ClothesType.OUTER)
                .imageUrl("imageUrl")
                .thickness("두꺼움")
                .color("블랙")
                .season("겨울")
                .temperatureCurrent(5.0)
                .windSpeed(2.0)
                .humidityCurrent(40.0)
                .precipitationAmount(0.0)
                .temperatureSensitivity(0)
                .build();

        // when
        double score = clothesCalculator.calculateScore(clothes);

        // then
        assertThat(score).isGreaterThan(50.0); // 점수가 높을 것으로 예상
    }

    @Test
    @DisplayName("비 오는 날씨 점수 계산 테스트")
    void calculateScore_RainyWeather() {
        // given
        ScoredClothesDto clothes = ScoredClothesDto.builder()
                .id(UUID.randomUUID())
                .ownerId(UUID.randomUUID())
                .name("Rainy Weather Clothes")
                .type(ClothesType.OUTER)
                .imageUrl("imageUrl")
                .thickness("보통")
                .color("네이비")
                .season("가을")
                .temperatureCurrent(15.0)
                .windSpeed(1.0)
                .humidityCurrent(80.0)
                .precipitationAmount(5.0)
                .temperatureSensitivity(0)
                .build();

        // when
        double score = clothesCalculator.calculateScore(clothes);

        // then
        assertThat(score).isNotZero();
    }
}