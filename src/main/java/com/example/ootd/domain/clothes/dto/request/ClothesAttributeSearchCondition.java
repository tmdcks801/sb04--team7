package com.example.ootd.domain.clothes.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ClothesAttributeSearchCondition(
    String cursor,
    UUID idAfter,
    @NotNull(message = "limit은 필수입니다.")
    Integer limit,
    String sortBy,
    String sortDirection,
    String keywordLike
) {

}
