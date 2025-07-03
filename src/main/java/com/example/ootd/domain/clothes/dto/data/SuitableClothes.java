package com.example.ootd.domain.clothes.dto.data;

import java.util.UUID;
import lombok.Builder;

@Builder
public record SuitableClothes(
    UUID clothesId,
    String name,
    String type,
    String imageUrl,
    int score
) {

}
