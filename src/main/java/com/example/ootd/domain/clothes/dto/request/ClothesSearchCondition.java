package com.example.ootd.domain.clothes.dto.request;

import com.example.ootd.domain.clothes.entity.ClothesType;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record ClothesSearchCondition(
    String cursor,
    UUID idAfter,
    @NotBlank(message = "limit은 필수입니다.")
    Integer limit,
    ClothesType typeEqual,
    @NotBlank(message = "사용자 id는 필수입니다.")
    UUID ownerId
) {

}
