package com.example.ootd.domain.clothes.dto.request;

import com.example.ootd.domain.clothes.dto.data.ClothesAttributeDto;
import com.example.ootd.domain.clothes.entity.ClothesType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "의상 수정 요청")
public record ClothesUpdateRequest(
    @Schema(description = "의상 이름")
    String name,
    @Schema(description = "의상 타입")
    ClothesType type,
    @Schema(description = "의상 속성")
    List<ClothesAttributeDto> attributes
) {

}
