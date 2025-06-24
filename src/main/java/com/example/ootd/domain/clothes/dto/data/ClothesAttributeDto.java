package com.example.ootd.domain.clothes.dto.data;

import java.util.UUID;
import lombok.Builder;

@Builder
public record ClothesAttributeDto(
    UUID definitionId,
    String value
) {

}
