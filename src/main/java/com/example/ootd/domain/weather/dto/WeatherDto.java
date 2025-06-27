package com.example.ootd.domain.weather.dto;

import com.example.ootd.domain.weather.entity.SkyStatus;
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

}
