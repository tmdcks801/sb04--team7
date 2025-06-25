package com.example.ootd.domain.weather.dto;


import com.example.ootd.domain.weather.entity.Precipitation;
import com.example.ootd.domain.weather.entity.SkyStatus;
import com.example.ootd.domain.weather.entity.Temperature;
import com.example.ootd.domain.weather.entity.Weather;
import java.util.UUID;

//@Schema(description = "피드용 요약 날씨 정보 DTO")
public record WeatherSummaryDto(

//    @Schema(description = "날씨 ID")
    UUID weatherId,

//    @Schema(description = "하늘 상태")
    SkyStatus skyStatus,

//    @Schema(description = "강수 정보")
    PrecipitationDto precipitation,

//    @Schema(description = "온도 정보")
    TemperatureDto temperature

) {

  public static WeatherSummaryDto from(UUID id, SkyStatus status, Precipitation precipitation,
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

  public static WeatherSummaryDto from(Weather wheather) {
    return from(
        wheather.getId(),
        wheather.getSkyStatus(),
        wheather.getPrecipitation(),
        wheather.getTemperature()
    );
  }
}