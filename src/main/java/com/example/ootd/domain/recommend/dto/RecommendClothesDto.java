package com.example.ootd.domain.recommend.dto;

import com.example.ootd.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.example.ootd.domain.clothes.entity.ClothesType;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record RecommendClothesDto(
    UUID clothesId,
    String name,
    String imageUrl,
    ClothesType type,
    List<ClothesAttributeWithDefDto> attributes
) {

}
