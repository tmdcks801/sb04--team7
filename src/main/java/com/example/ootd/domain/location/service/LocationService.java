package com.example.ootd.domain.location.service;

import com.example.ootd.domain.location.dto.WeatherAPILocation;

public interface LocationService {

  WeatherAPILocation getGridAndLocation(double latitude, double longitude);

}
