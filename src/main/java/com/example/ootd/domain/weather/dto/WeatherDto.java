package com.example.ootd.domain.weather.dto;

import com.example.ootd.domain.location.entity.Location;
import com.example.ootd.domain.weather.entity.SkyStatus;
import com.example.ootd.domain.weather.entity.Weather;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record WeatherDto(

//    @Schema(description = "날씨 ID")
    UUID id,

//    @Schema(description = "예보가 수집된 시각")
    LocalDateTime forecastedAt,

//    @Schema(description = "해당 예보가 적용되는 시각")
    LocalDateTime forecastAt,

//    @Schema(description = "행정동 이름")
    List<String> locationNames,

//    @Schema(description = "하늘 상태", example = "CLEAR")
    SkyStatus skyStatus,

//    @Schema(description = "온도 정보")
    TemperatureDto temperature,

//    @Schema(description = "강수 정보")
    PrecipitationDto precipitation,

//    @Schema(description = "습도 정보")
    HumidityDto humidity,

//    @Schema(description = "풍속 정보")
    WindSpeedDto windSpeed

) {

  public static WeatherDto from(Weather weather, Location location) {
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
