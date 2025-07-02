package com.example.ootd.domain.location.mapper;

import com.example.ootd.domain.location.Location;
import com.example.ootd.domain.location.dto.WeatherAPILocation;

public class WeatherLocationMapper {

  public static Location toEntity(WeatherAPILocation dto) {
    return new Location(
        dto.latitude(),
        dto.longitude(),
        dto.x(),
        dto.y(),
        dto.locationNames()
    );
  }
}