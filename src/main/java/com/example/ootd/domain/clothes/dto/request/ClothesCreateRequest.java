package com.example.ootd.domain.clothes.dto.request;

import com.example.ootd.domain.clothes.dto.data.ClothesAttributeDto;
import com.example.ootd.domain.clothes.entity.ClothesType;
import java.util.List;
import java.util.UUID;

public record ClothesCreateRequest(
    UUID ownerId,
    String name,
    ClothesType type,
    List<ClothesAttributeDto> attributes
) {

}
