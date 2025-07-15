package com.example.ootd.domain.weather.service;

import com.example.ootd.domain.location.dto.WeatherAPILocation;
import com.example.ootd.domain.location.service.LocationService;
import com.example.ootd.domain.weather.dto.WeatherDto;
import com.example.ootd.domain.weather.dto.WeatherSummaryDto;
import com.example.ootd.domain.weather.entity.*;
import com.example.ootd.domain.weather.repository.WeatherRepository;
import com.example.ootd.exception.weather.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeatherServiceImpl 테스트")
class WeatherServiceImplTest {

    @Mock
    private LocationService locationService;

    @Mock
    private WeatherRepository weatherRepository;

    @InjectMocks
    private WeatherServiceImpl weatherService;

    private WeatherAPILocation mockLocation;
    private Weather mockWeather;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2024, 12, 15, 14, 0);

        mockLocation = new WeatherAPILocation(
            35.1595, 129.0756, 98, 76,
            List.of("부산광역시", "남구", "대연동", "")
        );

        mockWeather = createMockWeather("부산광역시 남구", testDateTime);
    }

    @Test
    @DisplayName("정상적인 좌표로 3일 날씨 조회 성공")
    void getThreeDayWeather_Success() {
        // Given
        double latitude = 35.1595;
        double longitude = 129.0756;

        List<Weather> mockWeatherList = createThreeDayWeatherData();

        when(locationService.getGridAndLocation(latitude, longitude))
            .thenReturn(mockLocation);
        when(weatherRepository.findWeathersByRegionAndDateRange(
            eq("부산광역시 남구"), any(), any()))
            .thenReturn(mockWeatherList);

        // When
        List<WeatherDto> result = weatherService.getThreeDayWeather(
            latitude, longitude, testDateTime);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).forecastAt().toLocalDate())
            .isEqualTo(testDateTime.toLocalDate());
        assertThat(result.get(1).forecastAt().toLocalDate())
            .isEqualTo(testDateTime.toLocalDate().plusDays(1));
        assertThat(result.get(2).forecastAt().toLocalDate())
            .isEqualTo(testDateTime.toLocalDate().plusDays(2));

        verify(locationService).getGridAndLocation(latitude, longitude);
        verify(weatherRepository).findWeathersByRegionAndDateRange(
            eq("부산광역시 남구"), any(), any());
    }

    @Test
    @DisplayName("잘못된 좌표 입력시 InvalidCoordinatesException 발생")
    void getThreeDayWeather_InvalidCoordinates() {
        // Given
        double invalidLatitude = 91.0; // 위도 범위 초과
        double longitude = 129.0756;

        // When & Then
        assertThatThrownBy(() ->
            weatherService.getThreeDayWeather(invalidLatitude, longitude, testDateTime))
            .isInstanceOf(InvalidCoordinatesException.class);
    }

    @Test
    @DisplayName("지역명이 부족한 경우 WeatherNotFoundException 발생")
    void getThreeDayWeather_InsufficientLocationNames() {
        // Given
        double latitude = 35.1595;
        double longitude = 129.0756;

        WeatherAPILocation insufficientLocation = new WeatherAPILocation(
            latitude, longitude, 98, 76,
            List.of("부산광역시") // 부족한 지역명
        );

        when(locationService.getGridAndLocation(latitude, longitude))
            .thenReturn(insufficientLocation);

        // When & Then
        assertThatThrownBy(() ->
            weatherService.getThreeDayWeather(latitude, longitude, testDateTime))
            .isInstanceOf(WeatherNotFoundException.class);
    }

    @Test
    @DisplayName("날씨 데이터가 없는 경우 WeatherNotFoundException 발생")
    void getThreeDayWeather_NoWeatherData() {
        // Given
        double latitude = 35.1595;
        double longitude = 129.0756;

        when(locationService.getGridAndLocation(latitude, longitude))
            .thenReturn(mockLocation);
        when(weatherRepository.findWeathersByRegionAndDateRange(
            any(), any(), any()))
            .thenReturn(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() ->
            weatherService.getThreeDayWeather(latitude, longitude, testDateTime))
            .isInstanceOf(WeatherNotFoundException.class);
    }

    @Test
    @DisplayName("충분하지 않은 날씨 데이터로 WeatherDataInsufficientException 발생")
    void getThreeDayWeather_InsufficientData() {
        // Given
        double latitude = 35.1595;
        double longitude = 129.0756;

        List<Weather> insufficientWeatherData = List.of(
            createMockWeather("부산광역시 남구", testDateTime)
        ); // 1일치만 제공

        when(locationService.getGridAndLocation(latitude, longitude))
            .thenReturn(mockLocation);
        when(weatherRepository.findWeathersByRegionAndDateRange(
            any(), any(), any()))
            .thenReturn(insufficientWeatherData);

        // When & Then
        assertThatThrownBy(() ->
            weatherService.getThreeDayWeather(latitude, longitude, testDateTime))
            .isInstanceOf(WeatherDataInsufficientException.class);
    }

    @Test
    @DisplayName("LocationService에서 예외 발생시 WeatherNotFoundException 전파")
    void getThreeDayWeather_LocationServiceException() {
        // Given
        double latitude = 35.1595;
        double longitude = 129.0756;

        when(locationService.getGridAndLocation(latitude, longitude))
            .thenThrow(new RuntimeException("Location service error"));

        // When & Then
        assertThatThrownBy(() ->
            weatherService.getThreeDayWeather(latitude, longitude, testDateTime))
            .isInstanceOf(WeatherNotFoundException.class);
    }

    @Test
    @DisplayName("getSummaryWeather 메소드는 현재 null 반환 (미구현)")
    void getSummaryWeather_ReturnsNull() {
        // When
        WeatherSummaryDto result = weatherService.getSummaryWeather(35.1595, 129.0756);

        // Then
        assertThat(result).isNull();
    }

    private Weather createMockWeather(String regionName, LocalDateTime forecastAt) {
        Weather weather = Weather.builder()
            .regionName(regionName)
            .forecastedAt(LocalDateTime.now())
            .forecastAt(forecastAt)
            .skyStatus(SkyStatus.CLEAR)
            .precipitation(createMockPrecipitation())
            .temperature(createMockTemperature())
            .humidity(createMockHumidity())
            .windSpeed(createMockWindSpeed())
            .build();

        ReflectionTestUtils.setField(weather, "id", UUID.randomUUID());
        return weather;
    }

    private List<Weather> createThreeDayWeatherData() {
        return Arrays.asList(
            // 오늘
            createMockWeather("부산광역시 남구", testDateTime),
            createMockWeather("부산광역시 남구", testDateTime.plusHours(3)),
            createMockWeather("부산광역시 남구", testDateTime.plusHours(6)),

            // 내일
            createMockWeather("부산광역시 남구", testDateTime.plusDays(1)),
            createMockWeather("부산광역시 남구", testDateTime.plusDays(1).plusHours(3)),
            createMockWeather("부산광역시 남구", testDateTime.plusDays(1).plusHours(6)),

            // 모레
            createMockWeather("부산광역시 남구", testDateTime.plusDays(2)),
            createMockWeather("부산광역시 남구", testDateTime.plusDays(2).plusHours(3)),
            createMockWeather("부산광역시 남구", testDateTime.plusDays(2).plusHours(6))
        );
    }

    private Precipitation createMockPrecipitation() {
        return Precipitation.builder()
            .precipitationType(PrecipitationType.NONE)
            .precipitationProbability(20.0)
            .precipitationAmount(0.0)
            .build();
    }

    private Temperature createMockTemperature() {
        return Temperature.builder()
            .temperatureCurrent(15.0)
            .temperatureMin(10.0)
            .temperatureMax(20.0)
            .temperatureComparedToDayBefore(-1.0)
            .build();
    }

    private Humidity createMockHumidity() {
        return Humidity.builder()
            .humidityCurrent(60.0)
            .humidityComparedToDayBefore(5.0)
            .build();
    }

    private WindSpeed createMockWindSpeed() {
        return WindSpeed.builder()
            .windSpeed(3.5)
            .windAsWord(WindStrength.WEAK)
            .build();
    }

    // === 추가된 확장 테스트들 ===

    @Test
    @DisplayName("정확한 시간 매칭 우선 - 오늘 날씨")
    void getThreeDayWeather_ExactTimeMatch_Today() {
        // Given
        double latitude = 35.1595;
        double longitude = 129.0756;

        // 정확한 시간의 날씨 데이터 생성
        Weather exactTimeWeather = createMockWeather("부산광역시 남구", testDateTime);
        Weather otherTimeWeather = createMockWeather("부산광역시 남구", testDateTime.plusHours(1));

        List<Weather> weatherList = Arrays.asList(
            exactTimeWeather, otherTimeWeather,
            createMockWeather("부산광역시 남구", testDateTime.plusDays(1)),
            createMockWeather("부산광역시 남구", testDateTime.plusDays(2))
        );

        when(locationService.getGridAndLocation(latitude, longitude))
            .thenReturn(mockLocation);
        when(weatherRepository.findWeathersByRegionAndDateRange(any(), any(), any()))
            .thenReturn(weatherList);

        // When
        List<WeatherDto> result = weatherService.getThreeDayWeather(latitude, longitude, testDateTime);

        // Then
        assertThat(result).hasSize(3);
        // 첫 번째 결과는 정확한 시간의 날씨여야 함
        assertThat(result.get(0).forecastAt()).isEqualTo(testDateTime);
    }

    @Test
    @DisplayName("가장 가까운 시간 매칭 - 정확한 시간이 없는 경우")
    void getThreeDayWeather_ClosestTimeMatch() {
        // Given
        double latitude = 35.1595;
        double longitude = 129.0756;
        LocalDateTime targetTime = testDateTime; // 14:00

        // 정확한 시간(14:00)은 없고 13:00과 15:00만 있음
        Weather closerTime = createMockWeather("부산광역시 남구", testDateTime.minusHours(1)); // 13:00
        Weather fartherTime = createMockWeather("부산광역시 남구", testDateTime.plusHours(1)); // 15:00

        List<Weather> weatherList = Arrays.asList(
            closerTime, fartherTime,
            createMockWeather("부산광역시 남구", testDateTime.plusDays(1)),
            createMockWeather("부산광역시 남구", testDateTime.plusDays(2))
        );

        when(locationService.getGridAndLocation(latitude, longitude))
            .thenReturn(mockLocation);
        when(weatherRepository.findWeathersByRegionAndDateRange(any(), any(), any()))
            .thenReturn(weatherList);

        // When
        List<WeatherDto> result = weatherService.getThreeDayWeather(latitude, longitude, targetTime);

        // Then
        assertThat(result).hasSize(3);
        // 더 가까운 시간의 날씨가 선택되어야 함
        assertThat(result.get(0).forecastAt()).isEqualTo(testDateTime.minusHours(1));
    }

    @Test
    @DisplayName("내일/모레 날씨 - 최악 강수 데이터 적용")
    void getThreeDayWeather_WorstPrecipitationForFutureDays() {
        // Given
        double latitude = 35.1595;
        double longitude = 129.0756;

        // 내일 여러 시간대의 날씨 (강수 확률이 다름)
        Weather tomorrowMorning = createWeatherWithPrecipitation("부산광역시 남구",
            testDateTime.plusDays(1).withHour(9), 20.0, 0.0, PrecipitationType.NONE);
        Weather tomorrowAfternoon = createWeatherWithPrecipitation("부산광역시 남구",
            testDateTime.plusDays(1).withHour(15), 80.0, 5.0, PrecipitationType.RAIN);
        Weather tomorrowEvening = createWeatherWithPrecipitation("부산광역시 남구",
            testDateTime.plusDays(1).withHour(21), 60.0, 2.0, PrecipitationType.RAIN);

        List<Weather> weatherList = Arrays.asList(
            createMockWeather("부산광역시 남구", testDateTime), // 오늘
            tomorrowMorning, tomorrowAfternoon, tomorrowEvening, // 내일
            createMockWeather("부산광역시 남구", testDateTime.plusDays(2)) // 모레
        );

        when(locationService.getGridAndLocation(latitude, longitude))
            .thenReturn(mockLocation);
        when(weatherRepository.findWeathersByRegionAndDateRange(any(), any(), any()))
            .thenReturn(weatherList);

        // When
        List<WeatherDto> result = weatherService.getThreeDayWeather(latitude, longitude, testDateTime);

        // Then
        assertThat(result).hasSize(3);

        // 내일 날씨는 최악의 강수 데이터(80%, 5.0mm, RAIN)가 적용되어야 함
        WeatherDto tomorrowWeather = result.get(1);
        assertThat(tomorrowWeather.precipitation().probability()).isEqualTo(0.8);
        assertThat(tomorrowWeather.precipitation().amount()).isEqualTo(5.0);
        assertThat(tomorrowWeather.precipitation().type()).isEqualTo(PrecipitationType.RAIN);
    }

    @Test
    @DisplayName("오늘 날씨 - 정확한 시간 데이터 그대로 사용")
    void getThreeDayWeather_TodayWeatherAsIs() {
        // Given
        double latitude = 35.1595;
        double longitude = 129.0756;

        // 오늘 정확한 시간의 날씨 (강수 확률 30%)
        Weather todayWeather = createWeatherWithPrecipitation("부산광역시 남구",
            testDateTime, 30.0, 1.0, PrecipitationType.RAIN);

        // 오늘 다른 시간의 날씨 (강수 확률 90% - 더 높음)
        Weather todayWorseWeather = createWeatherWithPrecipitation("부산광역시 남구",
            testDateTime.plusHours(3), 90.0, 10.0, PrecipitationType.RAIN);

        List<Weather> weatherList = Arrays.asList(
            todayWeather, todayWorseWeather,
            createMockWeather("부산광역시 남구", testDateTime.plusDays(1)),
            createMockWeather("부산광역시 남구", testDateTime.plusDays(2))
        );

        when(locationService.getGridAndLocation(latitude, longitude))
            .thenReturn(mockLocation);
        when(weatherRepository.findWeathersByRegionAndDateRange(any(), any(), any()))
            .thenReturn(weatherList);

        // When
        List<WeatherDto> result = weatherService.getThreeDayWeather(latitude, longitude, testDateTime);

        // Then
        assertThat(result).hasSize(3);

        // 오늘 날씨는 정확한 시간의 데이터 그대로 사용 (30%, 1.0mm)
        WeatherDto todayResult = result.get(0);
        assertThat(todayResult.precipitation().probability()).isEqualTo(0.3);
        assertThat(todayResult.precipitation().amount()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("강수 타입 우선순위 확인 - ordinal 값이 높을수록 우선")
    void getThreeDayWeather_PrecipitationTypePriority() {
        // Given
        double latitude = 35.1595;
        double longitude = 129.0756;

        // 내일 다양한 강수 타입의 날씨
        // NONE(0) < RAIN(1) < RAIN_SNOW(2) < SNOW(3) < SHOWER(4) 순서로 ordinal 값 증가
        // 강수 확률을 동일하게 만들어 타입으로만 비교하도록 함
        Weather rainWeather = createWeatherWithPrecipitation("부산광역시 남구",
            testDateTime.plusDays(1).withHour(9), 50.0, 3.0, PrecipitationType.RAIN);
        Weather snowWeather = createWeatherWithPrecipitation("부산광역시 남구",
            testDateTime.plusDays(1).withHour(15), 50.0, 3.0, PrecipitationType.SNOW); // 동일한 확률과 양
        Weather noneWeather = createWeatherWithPrecipitation("부산광역시 남구",
            testDateTime.plusDays(1).withHour(21), 50.0, 3.0, PrecipitationType.NONE); // 동일한 확률과 양

        List<Weather> weatherList = Arrays.asList(
            createMockWeather("부산광역시 남구", testDateTime), // 오늘
            rainWeather, snowWeather, noneWeather, // 내일
            createMockWeather("부산광역시 남구", testDateTime.plusDays(2)) // 모레
        );

        when(locationService.getGridAndLocation(latitude, longitude))
            .thenReturn(mockLocation);
        when(weatherRepository.findWeathersByRegionAndDateRange(any(), any(), any()))
            .thenReturn(weatherList);

        // When
        List<WeatherDto> result = weatherService.getThreeDayWeather(latitude, longitude, testDateTime);

        // Then
        // SNOW(ordinal=3)가 가장 높은 우선순위를 가져야 함
        WeatherDto tomorrowWeather = result.get(1);
        assertThat(tomorrowWeather.precipitation().type()).isEqualTo(PrecipitationType.SNOW);
    }

    @Test
    @DisplayName("강수 확률이 동일할 때 강수량으로 우선순위 결정")
    void getThreeDayWeather_PrecipitationAmountPriority() {
        // Given
        double latitude = 35.1595;
        double longitude = 129.0756;

        // 내일 강수 확률은 같지만 강수량이 다른 날씨
        Weather lightRain = createWeatherWithPrecipitation("부산광역시 남구",
            testDateTime.plusDays(1).withHour(9), 70.0, 2.0, PrecipitationType.RAIN);
        Weather heavyRain = createWeatherWithPrecipitation("부산광역시 남구",
            testDateTime.plusDays(1).withHour(15), 70.0, 8.0, PrecipitationType.RAIN);
        Weather mediumRain = createWeatherWithPrecipitation("부산광역시 남구",
            testDateTime.plusDays(1).withHour(21), 70.0, 5.0, PrecipitationType.RAIN);

        List<Weather> weatherList = Arrays.asList(
            createMockWeather("부산광역시 남구", testDateTime), // 오늘
            lightRain, heavyRain, mediumRain, // 내일
            createMockWeather("부산광역시 남구", testDateTime.plusDays(2)) // 모레
        );

        when(locationService.getGridAndLocation(latitude, longitude))
            .thenReturn(mockLocation);
        when(weatherRepository.findWeathersByRegionAndDateRange(any(), any(), any()))
            .thenReturn(weatherList);

        // When
        List<WeatherDto> result = weatherService.getThreeDayWeather(latitude, longitude, testDateTime);

        // Then
        // 강수량이 가장 많은 8.0mm가 선택되어야 함
        WeatherDto tomorrowWeather = result.get(1);
        assertThat(tomorrowWeather.precipitation().amount()).isEqualTo(8.0);
    }

    @Test
    @DisplayName("특정 날짜의 날씨 데이터가 없는 경우")
    void getThreeDayWeather_MissingDayData() {
        // Given
        double latitude = 35.1595;
        double longitude = 129.0756;

        // 오늘과 모레 데이터만 있고 내일 데이터가 없음
        List<Weather> weatherList = Arrays.asList(
            createMockWeather("부산광역시 남구", testDateTime), // 오늘
            // 내일 데이터 없음
            createMockWeather("부산광역시 남구", testDateTime.plusDays(2)) // 모레
        );

        when(locationService.getGridAndLocation(latitude, longitude))
            .thenReturn(mockLocation);
        when(weatherRepository.findWeathersByRegionAndDateRange(any(), any(), any()))
            .thenReturn(weatherList);

        // When & Then
        assertThatThrownBy(() ->
            weatherService.getThreeDayWeather(latitude, longitude, testDateTime))
            .isInstanceOf(WeatherDataInsufficientException.class);
    }

    @Test
    @DisplayName("극단적인 좌표값 처리")
    void getThreeDayWeather_ExtremeCoordinates() {
        // When & Then - 위도 극값
        assertThatThrownBy(() ->
            weatherService.getThreeDayWeather(-91.0, 127.0, testDateTime))
            .isInstanceOf(InvalidCoordinatesException.class);

        assertThatThrownBy(() ->
            weatherService.getThreeDayWeather(91.0, 127.0, testDateTime))
            .isInstanceOf(InvalidCoordinatesException.class);

        // When & Then - 경도 극값
        assertThatThrownBy(() ->
            weatherService.getThreeDayWeather(37.5, -181.0, testDateTime))
            .isInstanceOf(InvalidCoordinatesException.class);

        assertThatThrownBy(() ->
            weatherService.getThreeDayWeather(37.5, 181.0, testDateTime))
            .isInstanceOf(InvalidCoordinatesException.class);
    }

    // 강수 데이터를 커스터마이징할 수 있는 헬퍼 메서드
    private Weather createWeatherWithPrecipitation(String regionName, LocalDateTime forecastAt,
            double probability, double amount, PrecipitationType type) {
        Precipitation precipitation = Precipitation.builder()
            .precipitationType(type)
            .precipitationProbability(probability)
            .precipitationAmount(amount)
            .build();

        Weather weather = Weather.builder()
            .regionName(regionName)
            .forecastedAt(LocalDateTime.now())
            .forecastAt(forecastAt)
            .skyStatus(SkyStatus.CLEAR)
            .precipitation(precipitation)
            .temperature(createMockTemperature())
            .humidity(createMockHumidity())
            .windSpeed(createMockWindSpeed())
            .build();

        ReflectionTestUtils.setField(weather, "id", UUID.randomUUID());
        return weather;
    }
}
