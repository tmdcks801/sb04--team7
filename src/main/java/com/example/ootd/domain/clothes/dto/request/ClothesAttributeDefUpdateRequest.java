package com.example.ootd.domain.clothes.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "의상 속성 정의 수정 요청")
public record ClothesAttributeDefUpdateRequest(
    @Schema(description = "속성 정의 이름")
    String name,
    @Schema(description = "선택 가능한 속성 값 목록")
    List<String> selectableValues
) {

}
