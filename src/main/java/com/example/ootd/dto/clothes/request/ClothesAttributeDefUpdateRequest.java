package com.example.ootd.dto.clothes.request;

import java.util.List;

public record ClothesAttributeDefUpdateRequest(
    String name,
    List<String> selectableValue
) {

}
