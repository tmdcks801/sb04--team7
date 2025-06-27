package com.example.ootd.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

  private static final String DELIMITER = ",";

  @Override
  public String convertToDatabaseColumn(List<String> attribute) {

    if (attribute == null || attribute.isEmpty()) {
      return "";
    }

    return String.join(DELIMITER, attribute);
  }


  @Override
  public List<String> convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isBlank()) {
      return Collections.emptyList();
    }

    return Arrays.stream(dbData.split(DELIMITER))
        .map(String::trim)
        .collect(Collectors.toList());
  }

  /**
   * DB 저장 시 List를 String으로 변환
   */
  public static String serialize(List<String> attribute) {
    if (attribute == null || attribute.isEmpty()) {
      return "";
    }
    return String.join(DELIMITER, attribute);
  }

  /**
   * 조회 시 String을 List로 변환
   */
  public static List<String> deserialize(String dbData) {
    if (dbData == null || dbData.isBlank()) {
      return Collections.emptyList();
    }
    return Arrays.stream(dbData.split(DELIMITER))
        .map(String::trim)
        .collect(Collectors.toList());
  }
}
