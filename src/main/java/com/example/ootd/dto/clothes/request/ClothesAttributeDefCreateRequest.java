package com.example.ootd.dto.clothes.request;

import java.util.List;

public record ClothesAttributeDefCreateRequest(
    String name,
    List<String> selectableValue
) {

}
