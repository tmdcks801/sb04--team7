package com.example.ootd.domain.weather.service;

import com.example.ootd.domain.location.dto.WeatherAPILocation;
import com.example.ootd.domain.location.mapper.WeatherLocationMapper;
import com.example.ootd.domain.location.service.LocationService;
import com.example.ootd.domain.weather.dto.WeatherDto;
import com.example.ootd.domain.weather.dto.WeatherSummaryDto;
import com.example.ootd.domain.weather.entity.Weather;
import com.example.ootd.domain.weather.mapper.WeatherMapper;
import com.example.ootd.domain.weather.repository.WeatherRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class WeatherServiceImpl implements WeatherService {

  private final LocationService locationService;
  private final WeatherRepository weatherRepository;

  @Override
  public WeatherDto getWeather(double longitude, double latitude) {
    LocalDate today = LocalDate.now();
    LocalDateTime startDateTime = today.atStartOfDay();
    LocalDateTime endDateTime = today.plusDays(3).atTime(LocalTime.MAX);

    // 2. 위경도로 행정동 조회
    WeatherAPILocation location = locationService.getGridAndLocation(latitude, longitude);
    List<String> locationNames = location.locationNames();

    // 3. 지역명 병합
    if (locationNames == null || locationNames.size() < 3) {
      throw new IllegalArgumentException("지역 이름 정보가 부족하거나 누락되었습니다: " + locationNames);
    }
    String mergedRegionName = locationNames.get(1) + " " + locationNames.get(2);

    // 4. 날씨 데이터 조회
    Weather weather = weatherRepository
        .findMidnightWeathersByRegionNameWithLatestForecastedAt(mergedRegionName)
        .stream()
        .findFirst()
        .orElseThrow(() ->
            new IllegalArgumentException("해당 지역(" + mergedRegionName + ")의 날씨 정보를 찾을 수 없습니다."));

    // 5. 여기서 내일 모래 날씨

    return WeatherMapper.toDto(weather, WeatherLocationMapper.toEntity(location));
  }

  @Override
  public WeatherSummaryDto getSummaryWeather(double longitude, double latitude) {
    return null;
  }
}
