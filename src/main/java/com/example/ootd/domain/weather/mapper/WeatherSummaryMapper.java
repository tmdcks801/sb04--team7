package com.example.ootd.domain.weather.mapper;

import com.example.ootd.domain.weather.dto.PrecipitationDto;
import com.example.ootd.domain.weather.dto.TemperatureDto;
import com.example.ootd.domain.weather.dto.WeatherSummaryDto;
import com.example.ootd.domain.weather.entity.Precipitation;
import com.example.ootd.domain.weather.entity.SkyStatus;
import com.example.ootd.domain.weather.entity.Temperature;
import com.example.ootd.domain.weather.entity.Weather;
import java.util.UUID;

public class WeatherSummaryMapper {

  public static WeatherSummaryDto toDto(UUID id, SkyStatus status, Precipitation precipitation,
      Temperature temperature) {
    return new WeatherSummaryDto(
        id,
        status,
        new PrecipitationDto(
            precipitation.getPrecipitationType(),
            precipitation.getPrecipitationAmount(),
            precipitation.getPrecipitationProbability()
        ),
        new TemperatureDto(
            temperature.getTemperatureCurrent(),
            temperature.getTemperatureMin(),
            temperature.getTemperatureMax(),
            temperature.getTemperatureComparedToDayBefore()
        )
    );
  }

  public static WeatherSummaryDto toDto(Weather weather) {
    return toDto(
        weather.getId(),
        weather.getSkyStatus(),
        weather.getPrecipitation(),
        weather.getTemperature()
    );
  }
}
