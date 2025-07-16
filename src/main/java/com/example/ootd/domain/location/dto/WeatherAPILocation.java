package com.example.ootd.domain.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "기상청 단기예보용 격자 위치 및 행정동 정보")
public record WeatherAPILocation(
    @Schema(description = "위도")
    double latitude,
    @Schema(description = "경도")
    double longitude,
    @Schema(description = "격자 X")
    int x,
    @Schema(description = "격자 Y")
    int y,
    @Schema(description = "행정동 이름")
    List<String> locationNames
) {

}
