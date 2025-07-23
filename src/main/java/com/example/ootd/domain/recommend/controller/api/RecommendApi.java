package com.example.ootd.domain.recommend.controller.api;

import com.example.ootd.domain.recommend.dto.RecommendationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "추천 관리", description = "추천 관련 API")
public interface RecommendApi {

  @Operation(summary = "추천 조회", description = "추천 조회 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "추천 조회 성공",
          content = @Content(schema = @Schema(implementation = RecommendationDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "추천 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/api/recommendation")
  ResponseEntity<RecommendationDto> recommend(@RequestParam("weatherId") UUID weatherId);
}
