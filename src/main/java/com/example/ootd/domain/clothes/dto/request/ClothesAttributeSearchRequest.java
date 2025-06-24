package com.example.ootd.domain.clothes.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record ClothesAttributeSearchRequest(
    String cursor,
    UUID idAfter,
    @NotBlank(message = "limit은 필수입니다.")
    Integer limit,
    String sortBy,
    String sortDirection,
    String keywordLike
) {

}
