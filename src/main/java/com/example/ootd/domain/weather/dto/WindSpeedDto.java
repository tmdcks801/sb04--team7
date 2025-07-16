package com.example.ootd.domain.weather.dto;

import com.example.ootd.domain.weather.entity.WindStrength;
import io.swagger.v3.oas.annotations.media.Schema;

public record WindSpeedDto(
    @Schema(description = "풍속 (m/s)")
    double speed,
    @Schema(description = "풍속 강도")
    WindStrength asWord
) {

}