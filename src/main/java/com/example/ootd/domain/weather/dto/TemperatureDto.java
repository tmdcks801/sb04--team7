package com.example.ootd.domain.weather.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "온도 정보")
public record TemperatureDto(
    @Schema(description = "현재 온도")
    double current,
    @Schema(description = "최저 온도")
    double min,
    @Schema(description = "최고 온도")
    double max,
    @Schema(description = "전날 대비 온도 변화")
    double comparedToDayBefore
) {}
