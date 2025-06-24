package com.example.ootd.domain.weather.dto;

public record HumidityDto(
//    @Schema(description = "현재 습도")
    double current,
//    @Schema(description = "전날 대비 습도 변화")
    double comparedToDayBefore
) {}