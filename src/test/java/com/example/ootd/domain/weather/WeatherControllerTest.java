package com.example.ootd.domain.weather;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ootd.domain.location.dto.WeatherAPILocation;
import com.example.ootd.domain.location.service.LocationService;
import com.example.ootd.domain.weather.dto.HumidityDto;
import com.example.ootd.domain.weather.dto.PrecipitationDto;
import com.example.ootd.domain.weather.dto.TemperatureDto;
import com.example.ootd.domain.weather.dto.WeatherDto;
import com.example.ootd.domain.weather.dto.WindSpeedDto;
import com.example.ootd.domain.weather.entity.PrecipitationType;
import com.example.ootd.domain.weather.entity.SkyStatus;
import com.example.ootd.domain.weather.entity.WindStrength;
import com.example.ootd.domain.weather.service.WeatherService;
import com.example.ootd.exception.weather.InvalidCoordinatesException;
import com.example.ootd.exception.weather.WeatherNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("WeatherController 테스트")
class WeatherControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private LocationService locationService;

  @MockitoBean
  private WeatherService weatherService;

  @Test
  @WithMockUser  // 인증된 사용자로 테스트
  @DisplayName("정상적인 날씨 조회 요청")
  void getWeatherByLocation_Success() throws Exception {
    // Given
    double latitude = 35.1595;
    double longitude = 129.0756;

    List<WeatherDto> mockWeatherList = createMockWeatherDtoList();
    when(weatherService.getThreeDayWeather(eq(latitude), eq(longitude), any(LocalDateTime.class)))
        .thenReturn(mockWeatherList);

    // When & Then
    mockMvc.perform(get("/api/weathers")
            .param("latitude", String.valueOf(latitude))
            .param("longitude", String.valueOf(longitude))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0].locationNames[0]").value("부산광역시"))
        .andExpect(jsonPath("$[0].locationNames[1]").value("남구"));

    verify(weatherService).getThreeDayWeather(eq(latitude), eq(longitude),
        any(LocalDateTime.class));
  }

  @Test
  @WithMockUser
  @DisplayName("잘못된 좌표로 인한 400 에러")
  void getWeatherByLocation_InvalidCoordinates() throws Exception {
    // Given
    double invalidLatitude = 91.0;
    double longitude = 129.0756;

    when(weatherService.getThreeDayWeather(eq(invalidLatitude), eq(longitude),
        any(LocalDateTime.class)))
        .thenThrow(new InvalidCoordinatesException(invalidLatitude, longitude));

    // When & Then
    mockMvc.perform(get("/api/weathers")
            .param("latitude", String.valueOf(invalidLatitude))
            .param("longitude", String.valueOf(longitude))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  @DisplayName("날씨 데이터 없음으로 인한 404 에러")
  void getWeatherByLocation_WeatherNotFound() throws Exception {
    // Given
    double latitude = 35.1595;
    double longitude = 129.0756;

    when(weatherService.getThreeDayWeather(eq(latitude), eq(longitude), any(LocalDateTime.class)))
        .thenThrow(new WeatherNotFoundException());

    // When & Then
    mockMvc.perform(get("/api/weathers")
            .param("latitude", String.valueOf(latitude))
            .param("longitude", String.valueOf(longitude))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  @DisplayName("위치 정보 조회 성공")
  void getLocation_Success() throws Exception {
    // Given
    double latitude = 35.1595;
    double longitude = 129.0756;

    WeatherAPILocation mockLocation = new WeatherAPILocation(
        latitude, longitude, 98, 75,
        List.of("부산광역시", "남구", "대연동", "")
    );

    when(locationService.getGridAndLocation(latitude, longitude))
        .thenReturn(mockLocation);

    // When & Then
    mockMvc.perform(get("/api/weathers/location")
            .param("latitude", String.valueOf(latitude))
            .param("longitude", String.valueOf(longitude))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.latitude").value(latitude))
        .andExpect(jsonPath("$.longitude").value(longitude))
        .andExpect(jsonPath("$.x").value(98))
        .andExpect(jsonPath("$.y").value(75))
        .andExpect(jsonPath("$.locationNames[0]").value("부산광역시"))
        .andExpect(jsonPath("$.locationNames[1]").value("남구"));

    verify(locationService).getGridAndLocation(latitude, longitude);
  }

  private List<WeatherDto> createMockWeatherDtoList() {
    return List.of(
        createMockWeatherDto(LocalDateTime.now()),
        createMockWeatherDto(LocalDateTime.now().plusDays(1)),
        createMockWeatherDto(LocalDateTime.now().plusDays(2))
    );
  }

  private WeatherDto createMockWeatherDto(LocalDateTime forecastAt) {
    return new WeatherDto(
        UUID.randomUUID(),
        LocalDateTime.now(),
        forecastAt,
        List.of("부산광역시", "남구", "대연동", ""),
        SkyStatus.CLEAR,
        new TemperatureDto(15.0, 10.0, 20.0, -1.0),
        new PrecipitationDto(PrecipitationType.NONE, 0.0, 20.0),
        new HumidityDto(60.0, 5.0),
        new WindSpeedDto(3.5, WindStrength.WEAK)
    );
  }
}