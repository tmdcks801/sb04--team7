package com.example.ootd.domain.llm.service;

import com.example.ootd.domain.clothes.entity.Clothes;
import com.example.ootd.domain.clothes.entity.ClothesAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ClothesSimplifier {

  public static Map<String, Object> simplify(Clothes clothes) {
    Map<String, Object> simplified = new HashMap<>();

    // 기본 정보
    simplified.put("clothesId", clothes.getId().toString());
    simplified.put("name", clothes.getName());
    simplified.put("type", clothes.getType().name());
    simplified.put("imageUrl", clothes.getImage() != null ? clothes.getImage().getUrl() : null);

    // 모든 속성 정보를 정확한 구조로 포함
    List<Map<String, Object>> attributes = new ArrayList<>();
    for (ClothesAttribute attr : clothes.getClothesAttributes()) {
      Map<String, Object> attributeMap = new HashMap<>();
      attributeMap.put("definitionId", attr.getAttribute().getId().toString());
      attributeMap.put("definitionName", attr.getAttribute().getName());
      attributeMap.put("selectableValues", attr.getAttribute().getDetails());
      attributeMap.put("value", attr.getValue());
      attributes.add(attributeMap);
    }
    simplified.put("attributes", attributes);

    return simplified;
  }
}