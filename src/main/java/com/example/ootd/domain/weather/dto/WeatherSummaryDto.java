package com.example.ootd.domain.weather.dto;

import com.example.ootd.domain.weather.entity.SkyStatus;
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

}