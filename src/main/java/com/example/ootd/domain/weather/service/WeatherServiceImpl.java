package com.example.ootd.domain.weather.service;

import com.example.ootd.domain.location.dto.WeatherAPILocation;
import com.example.ootd.domain.location.mapper.WeatherLocationMapper;
import com.example.ootd.domain.location.service.LocationService;
import com.example.ootd.domain.weather.dto.WeatherDto;
import com.example.ootd.domain.weather.dto.WeatherSummaryDto;
import com.example.ootd.domain.weather.entity.Weather;
import com.example.ootd.domain.weather.mapper.WeatherMapper;
import com.example.ootd.domain.weather.repository.WeatherRepository;
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

    WeatherAPILocation location = locationService.getGridAndLocation(latitude, longitude);
    List<String> locationNames = location.locationNames();

    String mergedRegionName = "";
    if (locationNames.size() > 2) {
      mergedRegionName = locationNames.get(1) + " " + locationNames.get(2);
    } else {
      throw new IllegalArgumentException("지역 이름 정보가 부족합니다: " + locationNames);
    }

    Weather weather = weatherRepository.findFirstByRegionNameOrderByForecastedAtDesc(
            mergedRegionName)
        .orElseThrow(
            () -> new IllegalArgumentException("해당 지역에 대한 날씨 정보가 없습니다: "));

    return WeatherMapper.toDto(weather, WeatherLocationMapper.toEntity(location));
  }

  @Override
  public WeatherSummaryDto getSummaryWeather(double longitude, double latitude) {
    return null;
  }
}
