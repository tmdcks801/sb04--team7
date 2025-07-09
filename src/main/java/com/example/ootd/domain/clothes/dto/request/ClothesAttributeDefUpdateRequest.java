package com.example.ootd.domain.clothes.dto.request;

import java.util.List;

public record ClothesAttributeDefUpdateRequest(
    String name,
    List<String> selectableValues
) {

}
