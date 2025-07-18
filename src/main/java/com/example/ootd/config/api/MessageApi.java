package com.example.ootd.config.api;

import com.example.ootd.domain.message.dto.DirectMessageRequest;
import com.example.ootd.domain.message.dto.MessagePaginationRequest;
import com.example.ootd.dto.PageResponse;
import com.example.ootd.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "message 관리", description = "message 관련 API")
public interface MessageApi {

  @Operation(summary = "messages 가져오기", description = "messages 가져오기 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "notifications 가져오기 성공"),
      @ApiResponse(
          responseCode = "400",
          description = "message 가져오기 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @RequestMapping("/api/direct-messages")
  ResponseEntity<PageResponse> getMessage(
      @AuthenticationPrincipal(expression = "user.id") UUID ownerId,
      @Valid @ModelAttribute MessagePaginationRequest request);

}
