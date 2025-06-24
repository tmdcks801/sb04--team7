package com.example.ootd.dto.clothes.data;

import java.util.List;
import java.util.UUID;

public record ClothesAttributeDefDto(
    UUID id,
    String name,
    List<String> selectableValue
) {

}
