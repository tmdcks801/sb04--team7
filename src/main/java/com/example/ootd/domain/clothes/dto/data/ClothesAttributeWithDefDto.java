package com.example.ootd.domain.clothes.dto.data;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ClothesAttributeWithDefDto(
    UUID definitionId,
    String definitionName,
    List<String> selectableValues,
    String value
) {

}
