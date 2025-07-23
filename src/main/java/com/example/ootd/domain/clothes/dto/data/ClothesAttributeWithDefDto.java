package com.example.ootd.domain.clothes.dto.data;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "의상 속성 정의 및 의상 속성 정보")
public record ClothesAttributeWithDefDto(
    @Schema(description = "속성 정의 id")
    UUID definitionId,
    @Schema(description = "속성 정의 이름")
    String definitionName,
    @Schema(description = "선택 가능한 속성 값 목록")
    List<String> selectableValues,
    @Schema(description = "속성 값")
    String value
) {

}
