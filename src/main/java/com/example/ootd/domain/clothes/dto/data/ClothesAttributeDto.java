package com.example.ootd.domain.clothes.dto.data;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "의상 속성 정보")
public record ClothesAttributeDto(
    @Schema(description = "속성 정의 id")
    UUID definitionId,
    @Schema(description = "속성 값")
    String value
) {

}
