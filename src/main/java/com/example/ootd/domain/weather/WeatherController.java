package com.example.ootd.domain.weather;

import com.example.ootd.domain.location.dto.WeatherAPILocation;
import com.example.ootd.domain.location.service.LocationService;
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

  @GetMapping("/location")
  public ResponseEntity<WeatherAPILocation> getWeatherByLocation(
      @RequestParam(required = true) double longitude,
      @RequestParam(required = true) double latitude) {
    return ResponseEntity.ok(
        locationService.getGridAndLocation(latitude, longitude));
  }
}
