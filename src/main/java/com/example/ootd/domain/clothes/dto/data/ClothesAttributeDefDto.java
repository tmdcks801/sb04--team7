package com.example.ootd.domain.clothes.dto.data;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ClothesAttributeDefDto(
    UUID id,
    String name,
    List<String> selectableValue
) {

}
