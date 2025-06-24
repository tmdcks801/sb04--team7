package com.example.ootd.dto.clothes.request;

import com.example.ootd.domain.clothes.ClothesType;
import com.example.ootd.dto.clothes.data.ClothesAttributeDto;
import java.util.List;

public record ClothesUpdateRequest(
    String name,
    ClothesType type,
    List<ClothesAttributeDto> attributes
) {

}
