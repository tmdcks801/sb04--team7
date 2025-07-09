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
}
