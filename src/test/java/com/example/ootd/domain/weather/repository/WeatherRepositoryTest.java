package com.example.ootd.domain.weather.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ootd.config.TestConfig;
import com.example.ootd.domain.weather.entity.Humidity;
import com.example.ootd.domain.weather.entity.Precipitation;
import com.example.ootd.domain.weather.entity.PrecipitationType;
import com.example.ootd.domain.weather.entity.SkyStatus;
import com.example.ootd.domain.weather.entity.Temperature;
import com.example.ootd.domain.weather.entity.Weather;
import com.example.ootd.domain.weather.entity.WindSpeed;
import com.example.ootd.domain.weather.entity.WindStrength;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@DisplayName("WeatherRepository 테스트")
class WeatherRepositoryTest {

  @MockitoBean
  private com.example.ootd.domain.image.service.S3Service s3Service;

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private WeatherRepository weatherRepository;

  private LocalDateTime baseTime;
  private String testRegion;

  @BeforeEach
  void setUp() {
    baseTime = LocalDateTime.of(2024, 12, 15, 0, 0);
    testRegion = "부산광역시 남구";
  }

  @Test
  @DisplayName("지역별 자정 시간대 최신 예보 데이터 조회")
  void findMidnightWeathersByRegionNameWithLatestForecastedAt() {
    // Given
    LocalDateTime latestForecastedAt = baseTime.plusHours(2);
    LocalDateTime olderForecastedAt = baseTime;

    // 최신 예보 시간 데이터
    Weather latestMidnight1 = createAndSaveWeather(
        testRegion, latestForecastedAt, baseTime);
    Weather latestMidnight2 = createAndSaveWeather(
        testRegion, latestForecastedAt, baseTime.plusDays(1));

    // 구 예보 시간 데이터 (조회되지 않아야 함)
    createAndSaveWeather(testRegion, olderForecastedAt, baseTime);

    // 자정이 아닌 시간 데이터 (조회되지 않아야 함)
    createAndSaveWeather(testRegion, latestForecastedAt, baseTime.plusHours(6));

    entityManager.flush();
    entityManager.clear();

    // When
    List<Weather> result = weatherRepository
        .findMidnightWeathersByRegionNameWithLatestForecastedAt(testRegion);

    // Then
    assertThat(result).hasSize(2);
    assertThat(result).extracting(Weather::getForecastedAt)
        .allMatch(time -> time.equals(latestForecastedAt));
    assertThat(result).extracting(w -> w.getForecastAt().getHour())
        .allMatch(hour -> hour == 0);
  }

  @Test
  @DisplayName("지역별 기간 범위 날씨 데이터 조회")
  void findWeathersByRegionAndDateRange() {
    // Given
    LocalDateTime forecastedAt = baseTime.plusHours(2);
    LocalDateTime startDate = baseTime;
    LocalDateTime endDate = baseTime.plusDays(2).withHour(23).withMinute(59);

    // 범위 내 데이터
    Weather weather1 = createAndSaveWeather(testRegion, forecastedAt, baseTime);
    Weather weather2 = createAndSaveWeather(testRegion, forecastedAt, baseTime.plusDays(1));
    Weather weather3 = createAndSaveWeather(testRegion, forecastedAt, baseTime.plusDays(2));

    // 범위 외 데이터 (조회되지 않아야 함)
    createAndSaveWeather(testRegion, forecastedAt, baseTime.minusDays(1));
    createAndSaveWeather(testRegion, forecastedAt, baseTime.plusDays(3));

    // 다른 지역 데이터 (조회되지 않아야 함)
    createAndSaveWeather("서울특별시 강남구", forecastedAt, baseTime);

    entityManager.flush();
    entityManager.clear();

    // When
    List<Weather> result = weatherRepository.findWeathersByRegionAndDateRange(
        testRegion, startDate, endDate);

    // Then
    assertThat(result).hasSize(3);
    assertThat(result).extracting(Weather::getRegionName)
        .allMatch(region -> region.equals(testRegion));
    assertThat(result).extracting(Weather::getForecastAt)
        .allMatch(time -> !time.isBefore(startDate) && !time.isAfter(endDate));

    // 시간 순 정렬 확인
    assertThat(result).extracting(Weather::getForecastAt)
        .isSorted();
  }

  @Test
  @DisplayName("지역명과 예보 시간으로 날씨 데이터 조회")
  void findByRegionNameAndForecastAt() {
    // Given
    LocalDateTime forecastAt = baseTime.plusDays(1);
    Weather weather = createAndSaveWeather(testRegion, baseTime, forecastAt);

    entityManager.flush();
    entityManager.clear();

    // When
    var result = weatherRepository.findByRegionNameAndForecastAt(testRegion, forecastAt);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getRegionName()).isEqualTo(testRegion);
    assertThat(result.get().getForecastAt()).isEqualTo(forecastAt);
  }

  @Test
  @DisplayName("존재하지 않는 데이터 조회시 Optional.empty 반환")
  void findByRegionNameAndForecastAt_NotFound() {
    // When
    var result = weatherRepository.findByRegionNameAndForecastAt(
        "존재하지않는지역", baseTime);

    // Then
    assertThat(result).isEmpty();
  }

  private Weather createAndSaveWeather(String regionName,
      LocalDateTime forecastedAt,
      LocalDateTime forecastAt) {
    Weather weather = Weather.builder()
        .regionName(regionName)
        .forecastedAt(forecastedAt)
        .forecastAt(forecastAt)
        .skyStatus(SkyStatus.CLEAR)
        .precipitation(Precipitation.builder()
            .precipitationType(PrecipitationType.NONE)
            .precipitationProbability(20.0)
            .precipitationAmount(0.0)
            .build())
        .temperature(Temperature.builder()
            .temperatureCurrent(15.0)
            .temperatureMin(10.0)
            .temperatureMax(20.0)
            .temperatureComparedToDayBefore(-1.0)
            .build())
        .humidity(Humidity.builder()
            .humidityCurrent(60.0)
            .humidityComparedToDayBefore(5.0)
            .build())
        .windSpeed(WindSpeed.builder()
            .windSpeed(3.5)
            .windAsWord(WindStrength.WEAK)
            .build())
        .build();

    return entityManager.persistAndFlush(weather);
  }
}
