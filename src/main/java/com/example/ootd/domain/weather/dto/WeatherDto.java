package com.example.ootd.domain.weather.dto;

import com.example.ootd.domain.weather.entity.SkyStatus;
import com.example.ootd.domain.weather.entity.Wheather;
import java.time.LocalDateTime;
import java.util.UUID;

public record WeatherDto(

//    @Schema(description = "날씨 ID")
    UUID id,

//    @Schema(description = "예보가 수집된 시각")
    LocalDateTime forecastedAt,

//    @Schema(description = "해당 예보가 적용되는 시각")
    LocalDateTime forecastAt,

//    @Schema(description = "행정동 이름")
    String locationName,

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

  public static WeatherDto from(Wheather entity) {
    return new WeatherDto(
        entity.getId(),
        entity.getForecastedAt(),
        entity.getForecastAt(),
        entity.getLocation().getName(),
        entity.getSkyStatus(),
        new TemperatureDto(
            entity.getTemperature().getTemperatureCurrent(),
            entity.getTemperature().getTemperatureMin(),
            entity.getTemperature().getTemperatureMax(),
            entity.getTemperature().getTemperatureComparedToDayBefore()
        ),
        new PrecipitationDto(
            entity.getPrecipitation().getPrecipitationType(),
            entity.getPrecipitation().getPrecipitationAmount(),
            entity.getPrecipitation().getPrecipitationProbability()
        ),
        new HumidityDto(
            entity.getHumidity().getHumidityCurrent(),
            entity.getHumidity().getHumidityComparedToDayBefore()
        ),
        new WindSpeedDto(
            entity.getWindSpeed().getWindSpeed(),
            entity.getWindSpeed().getWindAsWord()
        )
    );
  }
}
