package com.example.ootd.domain.recommend.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record RecommendationDto(
    UUID weatherId,
    UUID userId,
    List<RecommendClothesDto> clothes
) {

}
