package com.example.ootd.domain.weather.mapper;

import com.example.ootd.domain.location.Location;
import com.example.ootd.domain.weather.dto.HumidityDto;
import com.example.ootd.domain.weather.dto.PrecipitationDto;
import com.example.ootd.domain.weather.dto.TemperatureDto;
import com.example.ootd.domain.weather.dto.WeatherDto;
import com.example.ootd.domain.weather.dto.WindSpeedDto;
import com.example.ootd.domain.weather.entity.Weather;

public class WeatherMapper {

  public static WeatherDto toDto(Weather weather, Location location) {
    return new WeatherDto(
        weather.getId(),
        weather.getForecastedAt(),
        weather.getForecastAt(),
        location.getLocationNames(),
        weather.getSkyStatus(),
        new TemperatureDto(
            weather.getTemperature().getTemperatureCurrent(),
            weather.getTemperature().getTemperatureMin(),
            weather.getTemperature().getTemperatureMax(),
            weather.getTemperature().getTemperatureComparedToDayBefore()
        ),
        new PrecipitationDto(
            weather.getPrecipitation().getPrecipitationType(),
            weather.getPrecipitation().getPrecipitationAmount(),
            weather.getPrecipitation().getPrecipitationProbability()
        ),
        new HumidityDto(
            weather.getHumidity().getHumidityCurrent(),
            weather.getHumidity().getHumidityComparedToDayBefore()
        ),
        new WindSpeedDto(
            weather.getWindSpeed().getWindSpeed(),
            weather.getWindSpeed().getWindAsWord()
        )
    );
  }
}