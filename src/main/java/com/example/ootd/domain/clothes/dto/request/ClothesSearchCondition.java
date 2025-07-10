package com.example.ootd.domain.clothes.dto.request;

import com.example.ootd.domain.clothes.entity.ClothesType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ClothesSearchCondition(
    String cursor,
    UUID idAfter,
    @NotNull(message = "limit은 필수입니다.")
    Integer limit,
    ClothesType typeEqual,
    @NotNull(message = "사용자 id는 필수입니다.")
    UUID ownerId
) {

}
