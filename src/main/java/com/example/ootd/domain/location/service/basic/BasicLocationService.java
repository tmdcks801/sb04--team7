package com.example.ootd.domain.location.service.basic;

import com.example.ootd.domain.location.dto.WeatherAPILocation;
import com.example.ootd.domain.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicLocationService implements LocationService {


  @Override
  public WeatherAPILocation getGridAndLocation(double latitude, double longitude) {
    return null;
  }
}
