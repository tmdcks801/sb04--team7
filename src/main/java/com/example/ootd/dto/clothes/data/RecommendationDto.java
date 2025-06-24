package com.example.ootd.dto.clothes.data;

import java.util.List;
import java.util.UUID;

public record RecommendationDto(
    UUID weatherId,
    UUID userId,
    List<ClothesDto> clothes
) {

}
