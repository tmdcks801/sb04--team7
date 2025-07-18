package com.example.ootd.config.api.dto;

import com.example.ootd.domain.clothes.dto.data.ClothesDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "의상 조회 응답")
public record ClothesPageResponse(
    @Schema(description = "의상 리스트")
    List<ClothesDto> data,
    @Schema(description = "다음 페이지 유무")
    boolean hasNext,
    @Schema(description = "다음 페이지에서 사용할 커서")
    Object nextCursor,
    @Schema(description = "다음 페이지에서 사용할 보조 커서")
    UUID nextIdAfter,
    @Schema(description = "정렬 기준")
    String sortBy,
    @Schema(description = "정렬 방향")
    String sortDirection,
    @Schema(description = "의상 총 개수")
    long totalCount
) {

}
