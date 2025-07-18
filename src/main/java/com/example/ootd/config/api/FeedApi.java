package com.example.ootd.config.api;

import com.example.ootd.config.api.dto.CommentPageResponse;
import com.example.ootd.config.api.dto.FeedPageResponse;
import com.example.ootd.domain.feed.dto.data.CommentDto;
import com.example.ootd.domain.feed.dto.data.FeedDto;
import com.example.ootd.domain.feed.dto.request.CommentCreateRequest;
import com.example.ootd.domain.feed.dto.request.FeedCommentSearchCondition;
import com.example.ootd.domain.feed.dto.request.FeedCreateRequest;
import com.example.ootd.domain.feed.dto.request.FeedSearchCondition;
import com.example.ootd.domain.feed.dto.request.FeedUpdateRequest;
import com.example.ootd.dto.PageResponse;
import com.example.ootd.exception.ErrorResponse;
import com.example.ootd.security.PrincipalUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

@Tag(name = "피드 관리", description = "피드 관련 API")
public interface FeedApi {

  @Operation(summary = "피드 목록 조회", description = "피드 목록 조회 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "피드 목록 조회 성공",
          content = @Content(schema = @Schema(implementation = FeedPageResponse.class))),
      @ApiResponse(
          responseCode = "400",
          description = "피드 목록 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<PageResponse<FeedDto>> findFeed(FeedSearchCondition condition,
      PrincipalUser principalUser);

  @Operation(summary = "피드 등록", description = "피드 등록 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "피드 등록 성공",
          content = @Content(schema = @Schema(implementation = FeedDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "피드 등록 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "404",
          description = "피드 등록 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<FeedDto> createFeed(FeedCreateRequest request);

  @Operation(summary = "피드 삭제", description = "피드 삭제 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "204",
          description = "피드 삭제 성공"),
      @ApiResponse(
          responseCode = "404",
          description = "피드 삭제 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<Void> deleteFeed(UUID feedId);

  @Operation(summary = "피드 수정", description = "피드 수정 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "피드 수정 성공",
          content = @Content(schema = @Schema(implementation = FeedDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "피드 수정 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "404",
          description = "피드 수정 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<FeedDto> updateFeed(UUID feedId, FeedUpdateRequest request,
      PrincipalUser principalUser);

  @Operation(summary = "피드 좋아요", description = "피드 좋아요 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "피드 좋아요 성공",
          content = @Content(schema = @Schema(implementation = FeedDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "피드 좋아요 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "404",
          description = "피드 좋아요 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "409",
          description = "피드 좋아요 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<FeedDto> createLike(UUID feedId, PrincipalUser principalUser);

  @Operation(summary = "피드 좋아요 취소", description = "피드 좋아요 취소 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "204",
          description = "피드 좋아요 취소 성공"),
      @ApiResponse(
          responseCode = "404",
          description = "피드 좋아요 취소 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<FeedDto> deleteLike(UUID feedId, PrincipalUser principalUser);

  @Operation(summary = "피드 댓글 목록 조회", description = "피드 댓글 목록 조회 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "피드 댓글 목록 조회 성공",
          content = @Content(schema = @Schema(implementation = CommentPageResponse.class))),
      @ApiResponse(
          responseCode = "400",
          description = "피드 댓글 목록 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<PageResponse<CommentDto>> findComment(UUID feedId,
      FeedCommentSearchCondition condition);

  @Operation(summary = "피드 댓글 등록", description = "피드 댓글 등록 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "피드 댓글 등록 성공",
          content = @Content(schema = @Schema(implementation = FeedDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "피드 댓글 등록 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "404",
          description = "피드 댓글 등록 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<CommentDto> createComment(CommentCreateRequest request,
      PrincipalUser principalUser);
}
