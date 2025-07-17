package com.example.ootd.config.api;

import com.example.ootd.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeSearchCondition;
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

@Tag(name = "의상 속성 정의 관리", description = "의상 속성 정의 관련 API")
public interface AttributeApi {

  @Operation(summary = "의상 속성 정의 목록 조회", description = "의상 속성 정의 목록 조회 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "의상 속성 정의 조회 성공",
          content = @Content(schema = @Schema(implementation = PageResponse.class))),
      @ApiResponse(
          responseCode = "400",
          description = "의상 속성 정의 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<PageResponse<ClothesAttributeDefDto>> find(
      ClothesAttributeSearchCondition condition);

  @Operation(summary = "의상 속성 정의 등록", description = "의상 속성 정의 등록 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "의상 속성 정의 등록 성공",
          content = @Content(schema = @Schema(implementation = ClothesAttributeDefDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "의상 속성 정의 등록 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "409",
          description = "의상 속성 정의 등록 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<ClothesAttributeDefDto> create(ClothesAttributeDefCreateRequest request);

  @Operation(summary = "의상 속성 정의 삭제", description = "의상 속성 정의 삭제 API")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "204",
          description = "의상 속성 정의 삭제 성공"
      ),
      @ApiResponse(
          responseCode = "404",
          description = "의상 속성 정의 삭제 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<Void> delete(UUID definitionId);

  @Operation(summary = "의상 속성 정의 수정", description = "의상 속성 정의 수정 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "의상 속성 정의 수정 성공",
          content = @Content(schema = @Schema(implementation = ClothesAttributeDefDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "의상 속성 정의 수정 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<ClothesAttributeDefDto> update(UUID definitionId,
      ClothesAttributeDefUpdateRequest request);
}
