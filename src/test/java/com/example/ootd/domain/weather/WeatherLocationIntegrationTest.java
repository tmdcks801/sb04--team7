package com.example.ootd.domain.weather;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ootd.domain.location.Location;
import com.example.ootd.domain.location.repository.LocationRepository;
import com.example.ootd.domain.weather.entity.Humidity;
import com.example.ootd.domain.weather.entity.Precipitation;
import com.example.ootd.domain.weather.entity.PrecipitationType;
import com.example.ootd.domain.weather.entity.SkyStatus;
import com.example.ootd.domain.weather.entity.Temperature;
import com.example.ootd.domain.weather.entity.Weather;
import com.example.ootd.domain.weather.entity.WindSpeed;
import com.example.ootd.domain.weather.entity.WindStrength;
import com.example.ootd.domain.weather.repository.WeatherRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@DisplayName("Weather & Location 통합 테스트")
class WeatherLocationIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private WeatherRepository weatherRepository;

  @Autowired
  private LocationRepository locationRepository;

  @MockitoBean
  private com.example.ootd.domain.location.api.KakaoApiClient kakaoApiClient;

  private Location testLocation;
  private LocalDateTime fixedForecastedAt; // 모든 데이터에 동일한 forecastedAt 사용

  @BeforeEach
  void setUp() {
    // 기존 데이터 삭제
    weatherRepository.deleteAll();
    locationRepository.deleteAll();

    // 테스트 위치 데이터 생성
    List<String> locationNames = List.of("부산광역시", "남구", "대연동", "");
    testLocation = new Location(35.1595, 129.0756, 98, 76, locationNames);
    locationRepository.save(testLocation);

    // 현재 시간을 정각으로 맞춤
    LocalDateTime now = LocalDateTime.now()
        .withMinute(0)
        .withSecond(0)
        .withNano(0);

    // 모든 날씨 데이터에 동일한 forecastedAt 사용 (최신 예보 시간)
    fixedForecastedAt = LocalDateTime.now();

    // 3일간의 날씨 데이터 생성
    for (int day = 0; day < 3; day++) {
      LocalDateTime dayTime = now.plusDays(day);

      // 각 시간대별로 데이터 생성
      for (int hour = 0; hour < 24; hour++) {
        LocalDateTime forecastTime = dayTime.withHour(hour);
        Weather weather = createTestWeatherWithFixedForecastedAt("부산광역시 남구", forecastTime);
        weatherRepository.save(weather);
      }
    }
  }

  @Test
  @DisplayName("실제 데이터베이스를 사용한 3일 날씨 조회 통합 테스트")
  void getThreeDayWeather_IntegrationTest() throws Exception {
    // Given
    double latitude = 35.1595;
    double longitude = 129.0756;

    // When & Then
    mockMvc.perform(get("/api/weathers")
            .param("latitude", String.valueOf(latitude))
            .param("longitude", String.valueOf(longitude))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0].locationNames[0]").value("부산광역시"))
        .andExpect(jsonPath("$[0].locationNames[1]").value("남구"))
        .andExpect(jsonPath("$[0].temperature").exists())
        .andExpect(jsonPath("$[0].precipitation").exists())
        .andExpect(jsonPath("$[0].humidity").exists())
        .andExpect(jsonPath("$[0].windSpeed").exists());
  }

  @Test
  @DisplayName("캐시된 위치 정보를 사용한 날씨 조회")
  void getWeather_UseCachedLocation() throws Exception {
    // Given - setUp에서 이미 위치 정보가 저장됨
    double latitude = 35.1595;
    double longitude = 129.0756;

    // When & Then
    mockMvc.perform(get("/api/weathers/location")
            .param("latitude", String.valueOf(latitude))
            .param("longitude", String.valueOf(longitude))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.latitude").value(latitude))
        .andExpect(jsonPath("$.longitude").value(longitude))
        .andExpect(jsonPath("$.x").value(98))
        .andExpect(jsonPath("$.y").value(76))
        .andExpect(jsonPath("$.locationNames[0]").value("부산광역시"));
  }

  @Test
  @DisplayName("존재하지 않는 지역의 날씨 조회시 404 에러")
  void getWeather_NonExistentRegion() throws Exception {
    // Given - 다른 좌표 (데이터 없음)
    double latitude = 37.5665;
    double longitude = 126.9780;

    // When & Then
    mockMvc.perform(get("/api/weathers")
            .param("latitude", String.valueOf(latitude))
            .param("longitude", String.valueOf(longitude))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("잘못된 좌표로 400 에러")
  void getWeather_InvalidCoordinates() throws Exception {
    // Given
    double invalidLatitude = 91.0;
    double longitude = 129.0756;

    // When & Then
    mockMvc.perform(get("/api/weathers")
            .param("latitude", String.valueOf(invalidLatitude))
            .param("longitude", String.valueOf(longitude))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("데이터베이스 트랜잭션 롤백 확인")
  void transactionRollback_Test() throws Exception {
    // Given
    long initialWeatherCount = weatherRepository.count();
    long initialLocationCount = locationRepository.count();

    // 테스트 중간에 데이터 추가
    Weather additionalWeather = createTestWeatherWithFixedForecastedAt("테스트지역",
        LocalDateTime.now());
    weatherRepository.save(additionalWeather);

  }

  private Weather createTestWeatherWithFixedForecastedAt(String regionName,
      LocalDateTime forecastAt) {
    return Weather.builder()
        .regionName(regionName)
        .forecastedAt(fixedForecastedAt) // 모든 데이터가 동일한 forecastedAt을 가짐
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
  }
}