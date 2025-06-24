package com.example.ootd.domain.clothes.dto.request;

import java.util.UUID;

public record ClothesAttributeSearchRequest(
    String cursor,
    UUID idAfter,
    Integer limit,
    String sortBy,
    String sortDirection,
    String keywordLike
) {

}
