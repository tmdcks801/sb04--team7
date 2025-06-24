package com.example.ootd.domain.clothes.dto.request;

import java.util.List;

public record ClothesAttributeDefCreateRequest(
    String name,
    List<String> selectableValue
) {

}
