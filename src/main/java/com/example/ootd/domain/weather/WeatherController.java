package com.example.ootd.domain.weather;

import com.example.ootd.config.api.WeatherApi;
import com.example.ootd.domain.location.dto.WeatherAPILocation;
import com.example.ootd.domain.location.service.LocationService;
import com.example.ootd.domain.weather.dto.WeatherDto;
import com.example.ootd.domain.weather.service.WeatherService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weathers")
@RequiredArgsConstructor
public class WeatherController implements WeatherApi {

  private final LocationService locationService;
  private final WeatherService weatherService;

  @Override
  @GetMapping
  public ResponseEntity<List<WeatherDto>> getWeather(
      @RequestParam(required = true) double longitude,
      @RequestParam(required = true) double latitude) {
    return ResponseEntity.ok(
        weatherService.getThreeDayWeather(latitude, longitude, LocalDateTime.now()));
  }

  @Override
  @GetMapping("/location")
  public ResponseEntity<WeatherAPILocation> getWeatherLocation(
      @RequestParam(required = true) double longitude,
      @RequestParam(required = true) double latitude) {
    return ResponseEntity.ok(
        locationService.getGridAndLocation(latitude, longitude));
  }
}
