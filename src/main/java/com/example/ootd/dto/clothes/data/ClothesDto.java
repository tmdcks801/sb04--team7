package com.example.ootd.dto.clothes.data;

import com.example.ootd.domain.clothes.ClothesType;
import java.util.List;
import java.util.UUID;

public record ClothesDto(
    UUID id,
    UUID ownerId,
    String name,
    String imageUrl,
    ClothesType type,
    List<ClothesAttributeWithDefDto> attributes
) {

}
