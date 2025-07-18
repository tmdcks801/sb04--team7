package com.example.ootd.domain.clothes.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "의상 속성 정의 검색 조건")
public record ClothesAttributeSearchCondition(
    @Schema(description = "커서 - 이전 페이지 마지막 요소 name 또는 createdAt")
    String cursor,
    @Schema(description = "보조 커서 - 이전 페이지 마지막 요소 id")
    UUID idAfter,
    @NotNull(message = "limit은 필수입니다.")
    @Schema(description = "조회할 항목 수")
    Integer limit,
    @NotBlank(message = "정렬 기준은 필수입니다.")
    @Schema(description = "정렬 기준")
    String sortBy,
    @NotBlank(message = "정렬 방향은 필수입니다.")
    @Schema(description = "정렬 방향")
    String sortDirection,
    @Schema(description = "검색어")
    String keywordLike
) {

}
