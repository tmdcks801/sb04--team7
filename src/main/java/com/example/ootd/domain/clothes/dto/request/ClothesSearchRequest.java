package com.example.ootd.domain.clothes.dto.request;

import java.util.UUID;

public record ClothesSearchRequest(
    String cursor,
    UUID idAfter,
    Integer limit,
    String typeEqual,
    UUID ownerId
) {

}
