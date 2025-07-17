package com.example.ootd.domain.clothes.dto.request;

import com.example.ootd.domain.clothes.dto.data.ClothesAttributeDto;
import com.example.ootd.domain.clothes.entity.ClothesType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "의상 등록 요청")
public record ClothesCreateRequest(
    @Schema(description = "요청자 id")
    UUID ownerId,
    @Schema(description = "의상 이름")
    String name,
    @Schema(description = "의상 타입")
    ClothesType type,
    @Schema(description = "의상 속성 목록")
    List<ClothesAttributeDto> attributes
) {

}
