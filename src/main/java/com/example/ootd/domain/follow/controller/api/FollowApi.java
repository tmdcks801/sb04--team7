package com.example.ootd.domain.follow.controller.api;

import com.example.ootd.domain.follow.dto.FollowCreateRequest;
import com.example.ootd.domain.follow.dto.FollowDto;
import com.example.ootd.domain.follow.dto.FollowSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "팔로우 관리", description = "팔로우 관련 API")
public interface FollowApi {

  @Operation(summary = "팔로우 생성", description = "팔로우 생성 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "팔로우 생성 성공",
          content = @Content(schema = @Schema(implementation = FollowDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "팔로우 생성 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/api/follows")
  ResponseEntity<FollowDto> createFollow(@RequestBody FollowCreateRequest request);

  @Operation(summary = "팔로우 요약 정보 조회", description = "팔로우 요약 정보 조회 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "팔로우 요약 정보 조회 성공",
          content = @Content(schema = @Schema(implementation = FollowSummaryDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "팔로우 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @GetMapping("api/follows/summary")
  ResponseEntity<FollowSummaryDto> getSummary(@RequestBody UUID userId);

  @Operation(summary = "팔로잉 목록 조회", description = "팔로잉 목록 조회 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "팔로잉 목록 조회 성공",
          content = @Content(schema = @Schema(implementation = FollowSummaryDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "팔로잉 목록 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/api/follows/followings")
  ResponseEntity<FollowSummaryDto> getFollowings(
      @RequestBody UUID followerId,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam int limit,
      @RequestParam(required = false) String nameLike
  );

  @Operation(summary = "팔로워 목록 조회", description = "팔로워 목록 조회 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "팔로워 목록 조회 성공",
          content = @Content(schema = @Schema(implementation = FollowSummaryDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "팔로워 목록 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/api/follows/followers")
  ResponseEntity<FollowSummaryDto> getFollowers(
      @RequestBody UUID followeeId,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam int limit,
      @RequestParam(required = false) String nameLike
  );

  @Operation(summary = "팔로우 취소", description = "팔로우 취소 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "204",
          description = "팔로우 취소 성공",
          content = @Content),
      @ApiResponse(
          responseCode = "400",
          description = "팔로우 취소 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @DeleteMapping("/api/follows/{followId}")
  ResponseEntity<Void> deleteFollow(@PathVariable UUID followId);
}
