package com.example.ootd.config.api;

import com.example.ootd.config.api.dto.ClothesPageResponse;
import com.example.ootd.domain.clothes.dto.data.ClothesDto;
import com.example.ootd.domain.clothes.dto.request.ClothesCreateRequest;
import com.example.ootd.domain.clothes.dto.request.ClothesSearchCondition;
import com.example.ootd.domain.clothes.dto.request.ClothesUpdateRequest;
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
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "의상 관리", description = "의상 관련 API")
public interface ClothesApi {

  @Operation(summary = "의상 목록 조회", description = "의상 목록 조회 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "의상 조회 성공",
          content = @Content(schema = @Schema(implementation = ClothesPageResponse.class))),
      @ApiResponse(
          responseCode = "400",
          description = "의상 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  ResponseEntity<PageResponse<ClothesDto>> find(ClothesSearchCondition condition);

  @Operation(summary = "의상 등록", description = "의상 등록 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "의상 등록 성공",
          content = @Content(schema = @Schema(implementation = ClothesDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "의상 등록 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "404",
          description = "의상 등록 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<ClothesDto> create(ClothesCreateRequest request, MultipartFile image,
      PrincipalUser principalUser);

  @Operation(summary = "의상 삭제", description = "의상 삭제 API")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "204",
          description = "의상 삭제 성공"
      ),
      @ApiResponse(
          responseCode = "404",
          description = "의상 삭제 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<Void> delete(UUID clothesId);

  @Operation(summary = "의상 수정", description = "의상 수정 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "의상 수정 성공",
          content = @Content(schema = @Schema(implementation = ClothesDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "의상 수정 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "404",
          description = "의상 수정 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<ClothesDto> update(UUID clothesId, ClothesUpdateRequest request,
      MultipartFile image);
}
