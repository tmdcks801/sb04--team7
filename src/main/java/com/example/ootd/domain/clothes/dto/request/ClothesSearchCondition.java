package com.example.ootd.domain.clothes.dto.request;

import com.example.ootd.domain.clothes.entity.ClothesType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "의상 검색 조건")
public record ClothesSearchCondition(
    @Schema(description = "커서 - 이전 페이지 마지막 요소 createdAt")
    String cursor,
    @Schema(description = "보조 커서 - 이전 페이지 마지막 요소 id")
    UUID idAfter,
    @NotNull(message = "limit은 필수입니다.")
    @Schema(description = "조회할 항목 수")
    Integer limit,
    @Schema(description = "의상 타입")
    ClothesType typeEqual,
    @NotNull(message = "사용자 id는 필수입니다.")
    @Schema(description = "사용자 id")
    UUID ownerId
) {

}
