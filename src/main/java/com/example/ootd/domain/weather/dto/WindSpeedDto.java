package com.example.ootd.domain.weather.dto;

import com.example.ootd.domain.weather.entity.WindStrength;

public record WindSpeedDto(
//    @Schema(description = "풍속 (m/s)")
    double speed,
//    @Schema(description = "풍속 강도")
    WindStrength asWord
) {}