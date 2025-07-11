package com.example.ootd.domain.recommend.dto;

import com.example.ootd.domain.clothes.dto.data.ClothesDto;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record RecommendationDto(
    UUID weatherId,
    UUID userId,
    List<ClothesDto> clothes
) {

}
