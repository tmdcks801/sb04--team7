package com.example.ootd.domain.llm.service;

import com.example.ootd.domain.clothes.entity.Clothes;
import com.example.ootd.domain.clothes.entity.ClothesAttribute;
import java.util.HashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ClothesSimplifier {

  public static Map<String, Object> simplify(Clothes clothes) {
    Map<String, Object> simplified = new HashMap<>();

    // 기본 정보
    simplified.put("name", clothes.getName());
    simplified.put("type", clothes.getType().name()); // 예: TOP, BOTTOM 등

    // 속성 매핑
    for (ClothesAttribute attr : clothes.getClothesAttributes()) {
      String name = attr.getAttribute().getName();  // "두께감", "색상" 등
      String value = attr.getValue();

      switch (name) {
        case "두께감" -> simplified.put("thickness", value);
        case "색상" -> simplified.put("color", value);
        case "계절" -> simplified.put("season", value);
        case "스타일" -> simplified.put("style", value);
        case "길이" -> simplified.put("length", value);
        default -> {
        }
      }
    }

    return simplified;
  }
}