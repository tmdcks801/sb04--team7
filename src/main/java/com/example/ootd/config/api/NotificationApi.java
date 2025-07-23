package com.example.ootd.config.api;

import com.example.ootd.dto.PageResponse;
import com.example.ootd.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "notification 관리", description = "notification 관련 API")
public interface NotificationApi {


  @Operation(summary = "notifications 가져오기", description = "notifications 가져오기 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "notifications 가져오기 성공"),
      @ApiResponse(
          responseCode = "400",
          description = "notifications 가져오기 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/api/notifications")
  ResponseEntity<PageResponse> getNotifications(
      @AuthenticationPrincipal(expression = "user.id") UUID receiverId,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, defaultValue = "20") int limit);


  @Operation(summary = "notifications 읽기", description = "notifications 읽기 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "notifications 읽기 성공"),
      @ApiResponse(
          responseCode = "400",
          description = "notifications 읽기 실패")
  })
  @DeleteMapping("/api/notifications/{notificationId}")
  ResponseEntity<Void> readNotification(@PathVariable String notificationId);

}
