package com.example.ootd.config.api;

import com.example.ootd.domain.location.dto.WeatherAPILocation;
import com.example.ootd.domain.weather.dto.WeatherDto;
import com.example.ootd.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "날씨 관리", description = "날씨 관련 API")
public interface WeatherApi {

  @Operation(summary = "날씨 정보 조회", description = "날씨 정보 조회 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "날씨 조회 성공",
          content = @Content(schema = @Schema(implementation = WeatherDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "날씨 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/api/weathers")
  ResponseEntity<List<WeatherDto>> getWeather(
      @RequestParam double longitude,
      @RequestParam double latitude
  );

  @Operation(summary = "날씨 위치 정보 조회", description = "날씨 위치 정보 조회 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "날씨 위치 정보 조회 성공",
          content = @Content(schema = @Schema(implementation = WeatherAPILocation.class))),
      @ApiResponse(
          responseCode = "400",
          description = "날씨 위치 정보 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/api/weathers/location")
  ResponseEntity<WeatherAPILocation> getWeatherLocation(
      @RequestParam double longitude,
      @RequestParam double latitude
  );
}
