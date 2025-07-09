package com.example.ootd.batch.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.ootd.batch.dto.WeatherBatchData;
import com.example.ootd.batch.mapper.WeatherApiEntityMapper;
import com.example.ootd.domain.weather.api.WeatherApiResponse;
import com.example.ootd.domain.weather.entity.Humidity;
import com.example.ootd.domain.weather.entity.Precipitation;
import com.example.ootd.domain.weather.entity.PrecipitationType;
import com.example.ootd.domain.weather.entity.SkyStatus;
import com.example.ootd.domain.weather.entity.Temperature;
import com.example.ootd.domain.weather.entity.Weather;
import com.example.ootd.domain.weather.entity.WindSpeed;
import com.example.ootd.domain.weather.entity.WindStrength;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeatherProcessor 테스트")
class WeatherProcessorTest {

  @Mock
  private WeatherApiEntityMapper weatherApiEntityMapper;

  @InjectMocks
  private WeatherProcessor weatherProcessor;

  private WeatherBatchData testBatchData;
  private Weather baseWeather;
  private List<WeatherApiResponse.Item> currentItems;
  private List<WeatherApiResponse.Item> previousItems;

  @BeforeEach
  void setUp() {
    // 현재 날짜 기준으로 테스트 데이터 생성
    String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE);

    // 현재 날씨 API 응답 모킹
    currentItems = Arrays.asList(
        new WeatherApiResponse.Item(today, "1400", "TMP", today, "1400", "15", 98, 76),
        new WeatherApiResponse.Item(today, "1400", "REH", today, "1400", "60", 98, 76));

    // 이전 날씨 데이터 (전날 데이터와 TMN/TMX 포함)
    previousItems = Arrays.asList(
        // 전날 같은 시간대 데이터
        new WeatherApiResponse.Item(yesterday, "1400", "TMP", yesterday, "1400", "18", 98, 76),
        new WeatherApiResponse.Item(yesterday, "1400", "REH", yesterday, "1400", "55", 98, 76),
        // 오늘의 TMN/TMX 데이터
        new WeatherApiResponse.Item(today, "0600", "TMN", today, "0600", "8", 98, 76),
        new WeatherApiResponse.Item(today, "1500", "TMX", today, "1500", "22", 98, 76));

    testBatchData = WeatherBatchData.builder()
        .regionName("부산광역시 남구")
        .items(currentItems)
        .previousItems(previousItems)
        .build();

    // Temperature 객체 생성 - null 값으로 초기화하여 보완이 필요함을 나타냄
    Temperature temperature = Temperature.builder()
        .temperatureCurrent(15.0)
        .temperatureMin(null)  // null로 설정하여 TMN 보완 필요
        .temperatureMax(null)  // null로 설정하여 TMX 보완 필요
        .temperatureComparedToDayBefore(0.0)
        .build();

    // Humidity 객체 생성
    Humidity humidity = Humidity.builder()
        .humidityCurrent(60.0)
        .humidityComparedToDayBefore(0.0)
        .build();

    // Precipitation 객체 생성
    Precipitation precipitation = Precipitation.builder()
        .precipitationType(PrecipitationType.NONE)
        .precipitationProbability(20.0)
        .precipitationAmount(0.0)
        .build();

    // WindSpeed 객체 생성
    WindSpeed windSpeed = WindSpeed.builder()
        .windSpeed(3.5)
        .windAsWord(WindStrength.WEAK)
        .build();

    // 기본 Weather 엔티티 생성
    baseWeather = Weather.builder()
        .regionName("부산광역시 남구")
        .forecastAt(LocalDateTime.now().withHour(14).withMinute(0).withSecond(0).withNano(0))
        .forecastedAt(LocalDateTime.now())
        .skyStatus(SkyStatus.CLEAR)
        .temperature(temperature)
        .humidity(humidity)
        .precipitation(precipitation)
        .windSpeed(windSpeed)
        .build();
  }

  @Test
  @DisplayName("정상적인 날씨 데이터 처리")
  void process_Success() {
    // given
    when(weatherApiEntityMapper.toWeather(currentItems, "부산광역시 남구")).thenReturn(baseWeather);

    // when
    Weather result = weatherProcessor.process(testBatchData);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getRegionName()).isEqualTo("부산광역시 남구");

    // TMN/TMX 데이터가 보완되었는지 확인
    assertThat(result.getTemperature().getTemperatureMin()).isEqualTo(8.0);
    assertThat(result.getTemperature().getTemperatureMax()).isEqualTo(22.0);

    // 전날 대비 온도/습도 차이가 계산되었는지 확인
    assertThat(result.getTemperature().getTemperatureComparedToDayBefore()).isEqualTo(
        -3.0); // 15 - 18
    assertThat(result.getHumidity().getHumidityComparedToDayBefore()).isEqualTo(5.0); // 60 - 55
  }

  @Test
  @DisplayName("매퍼에서 null 반환 시 null 리턴")
  void process_MapperReturnsNull() {
    // given
    when(weatherApiEntityMapper.toWeather(currentItems, "부산광역시 남구")).thenReturn(null);

    // when
    Weather result = weatherProcessor.process(testBatchData);

    // then
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("이전 데이터가 없을 때 기본값 설정")
  void process_NoPreviousData() {
    // given
    WeatherBatchData dataWithoutPrevious = WeatherBatchData.builder()
        .regionName("부산광역시 남구")
        .items(currentItems)
        .previousItems(null)
        .build();

    // 새로운 Weather 객체 생성 (previousItems가 없을 때를 위한)
    Weather weatherForNoPrevious = Weather.builder()
        .regionName("부산광역시 남구")
        .forecastAt(LocalDateTime.now().withHour(14).withMinute(0).withSecond(0).withNano(0))
        .forecastedAt(LocalDateTime.now())
        .skyStatus(SkyStatus.CLEAR)
        .temperature(Temperature.builder()
            .temperatureCurrent(15.0)
            .temperatureMin(null)
            .temperatureMax(null)
            .temperatureComparedToDayBefore(0.0)
            .build())
        .humidity(Humidity.builder()
            .humidityCurrent(60.0)
            .humidityComparedToDayBefore(0.0)
            .build())
        .precipitation(Precipitation.builder()
            .precipitationType(PrecipitationType.NONE)
            .precipitationProbability(20.0)
            .precipitationAmount(0.0)
            .build())
        .windSpeed(WindSpeed.builder()
            .windSpeed(3.5)
            .windAsWord(WindStrength.WEAK)
            .build())
        .build();

    when(weatherApiEntityMapper.toWeather(currentItems, "부산광역시 남구")).thenReturn(
        weatherForNoPrevious);

    // when
    Weather result = weatherProcessor.process(dataWithoutPrevious);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTemperature().getTemperatureComparedToDayBefore()).isEqualTo(0.0);
    assertThat(result.getHumidity().getHumidityComparedToDayBefore()).isEqualTo(0.0);
    // TMN/TMX는 보완되지 않음
    assertThat(result.getTemperature().getTemperatureMin()).isNull();
    assertThat(result.getTemperature().getTemperatureMax()).isNull();
  }

  @Test
  @DisplayName("이미 TMN/TMX 데이터가 있는 경우 스킵")
  void process_TmnTmxAlreadyExists() {
    // given
    // TMN/TMX가 이미 설정된 Weather 객체 생성
    Weather weatherWithTmnTmx = Weather.builder()
        .regionName("부산광역시 남구")
        .forecastAt(LocalDateTime.now().withHour(14).withMinute(0).withSecond(0).withNano(0))
        .forecastedAt(LocalDateTime.now())
        .skyStatus(SkyStatus.CLEAR)
        .temperature(Temperature.builder()
            .temperatureCurrent(15.0)
            .temperatureMin(10.0)  // 이미 값이 있음
            .temperatureMax(25.0)  // 이미 값이 있음
            .temperatureComparedToDayBefore(0.0)
            .build())
        .humidity(Humidity.builder()
            .humidityCurrent(60.0)
            .humidityComparedToDayBefore(0.0)
            .build())
        .precipitation(Precipitation.builder()
            .precipitationType(PrecipitationType.NONE)
            .precipitationProbability(20.0)
            .precipitationAmount(0.0)
            .build())
        .windSpeed(WindSpeed.builder()
            .windSpeed(3.5)
            .windAsWord(WindStrength.WEAK)
            .build())
        .build();

    when(weatherApiEntityMapper.toWeather(currentItems, "부산광역시 남구")).thenReturn(weatherWithTmnTmx);

    // when
    Weather result = weatherProcessor.process(testBatchData);

    // then
    assertThat(result).isNotNull();
    // 기존 값이 유지되어야 함
    assertThat(result.getTemperature().getTemperatureMin()).isEqualTo(10.0);
    assertThat(result.getTemperature().getTemperatureMax()).isEqualTo(25.0);
  }

  @Test
  @DisplayName("잘못된 숫자 형식 처리")
  void process_InvalidNumberFormat() {
    // given
    String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE);

    List<WeatherApiResponse.Item> invalidItems = Arrays.asList(
        new WeatherApiResponse.Item(yesterday, "1400", "TMP", yesterday, "1400", "invalid", 98, 76),
        new WeatherApiResponse.Item(yesterday, "1400", "REH", yesterday, "1400", "not_number", 98,
            76));

    WeatherBatchData invalidData = WeatherBatchData.builder()
        .regionName("부산광역시 남구")
        .items(currentItems)
        .previousItems(invalidItems)
        .build();

    // 새로운 Weather 객체 생성
    Weather weatherForInvalid = Weather.builder()
        .regionName("부산광역시 남구")
        .forecastAt(LocalDateTime.now().withHour(14).withMinute(0).withSecond(0).withNano(0))
        .forecastedAt(LocalDateTime.now())
        .skyStatus(SkyStatus.CLEAR)
        .temperature(Temperature.builder()
            .temperatureCurrent(15.0)
            .temperatureMin(null)
            .temperatureMax(null)
            .temperatureComparedToDayBefore(0.0)
            .build())
        .humidity(Humidity.builder()
            .humidityCurrent(60.0)
            .humidityComparedToDayBefore(0.0)
            .build())
        .precipitation(Precipitation.builder()
            .precipitationType(PrecipitationType.NONE)
            .precipitationProbability(20.0)
            .precipitationAmount(0.0)
            .build())
        .windSpeed(WindSpeed.builder()
            .windSpeed(3.5)
            .windAsWord(WindStrength.WEAK)
            .build())
        .build();

    when(weatherApiEntityMapper.toWeather(currentItems, "부산광역시 남구")).thenReturn(weatherForInvalid);

    // when
    Weather result = weatherProcessor.process(invalidData);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTemperature().getTemperatureComparedToDayBefore()).isEqualTo(0.0);
    assertThat(result.getHumidity().getHumidityComparedToDayBefore()).isEqualTo(0.0);
  }

  @Test
  @DisplayName("예외 발생 시 null 반환")
  void process_ExceptionThrown() {
    // given
    when(weatherApiEntityMapper.toWeather(currentItems, "부산광역시 남구"))
        .thenThrow(new RuntimeException("Mapping error"));

    // when
    Weather result = weatherProcessor.process(testBatchData);

    // then
    assertThat(result).isNull();
  }
}