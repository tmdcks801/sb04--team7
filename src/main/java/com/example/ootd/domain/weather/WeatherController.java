package com.example.ootd.domain.weather;

import com.example.ootd.domain.location.dto.WeatherAPILocation;
import com.example.ootd.domain.location.service.LocationService;
import com.example.ootd.domain.weather.dto.WeatherDto;
import com.example.ootd.domain.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weathers")
@RequiredArgsConstructor
public class WeatherController {

  private final LocationService locationService;
  private final WeatherService weatherService;

  @GetMapping
  public ResponseEntity<WeatherDto> getWeatherByLocation(
      @RequestParam(required = true) double longitude,
      @RequestParam(required = true) double latitude) {
    return ResponseEntity.ok(
        weatherService.getWeather(latitude, longitude));
  }

  @GetMapping("/location")
  public ResponseEntity<WeatherAPILocation> getLocation(
      @RequestParam(required = true) double longitude,
      @RequestParam(required = true) double latitude) {
    return ResponseEntity.ok(
        locationService.getGridAndLocation(latitude, longitude));
  }
}
