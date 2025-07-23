package com.example.ootd.domain.clothes.dto.data;

import com.example.ootd.domain.clothes.entity.ClothesType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "추천된 의상 정보")
public record OotdDto(
    @Schema(description = "의상 id")
    UUID clothesId,
    @Schema(description = "의상 이름")
    String name,
    @Schema(description = "사진 url")
    String imageUrl,
    @Schema(description = "의상 타입")
    ClothesType type,
    @Schema(description = "의상 속성 목록")
    List<ClothesAttributeWithDefDto> attributes
) {

}
