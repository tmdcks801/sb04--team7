package com.example.ootd.domain.weather.dto;

import com.example.ootd.domain.weather.entity.PrecipitationType;
import io.swagger.v3.oas.annotations.media.Schema;

public record PrecipitationDto(
    @Schema(description = "강수 형태")
    PrecipitationType type,
    @Schema(description = "강수량")
    double amount,
    @Schema(description = "강수 확률")
    double probability
) {

}