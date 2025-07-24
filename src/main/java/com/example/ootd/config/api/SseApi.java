package com.example.ootd.config.api;

import com.example.ootd.domain.weather.dto.WeatherDto;
import com.example.ootd.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "SSE 관리", description = "SSE 관련 API")
public interface SseApi {

  @Operation(summary = "SSE 구독", description = "SSE 구독 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "SSE 구독 성공"),
      @ApiResponse(
          responseCode = "400",
          description = "SSE 구독 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/api/sse")
  SseEmitter subscribe(
      @RequestParam(value = "LastEventId", required = false) UUID lastEventId,
      @AuthenticationPrincipal(expression = "user.id") UUID receiverId);

}
