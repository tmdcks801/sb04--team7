package com.example.ootd.domain.recommend.service;

import com.example.ootd.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.example.ootd.domain.clothes.dto.data.ClothesDto;
import com.example.ootd.domain.recommend.dto.ScoredClothesDto;
import com.example.ootd.domain.clothes.entity.ClothesType;
import com.example.ootd.domain.clothes.repository.AttributeRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClothesSelector {

  private final AttributeRepository attributeRepository;


  public List<ClothesDto> selectRecommendedClothes(List<ScoredClothesDto> scoredClothes) {
    if (scoredClothes == null || scoredClothes.isEmpty()) {
      log.warn("scoredClothes가 비어있습니다.");
      return List.of();
    }

    // 타입별로 그룹화
    Map<ClothesType, List<ScoredClothesDto>> clothesByType = scoredClothes.stream()
        .collect(Collectors.groupingBy(ScoredClothesDto::type));

    List<ClothesDto> selectedClothes = new ArrayList<>();

    // 필수 아이템들
    addBestClothes(selectedClothes, clothesByType, ClothesType.TOP);
    addBestClothes(selectedClothes, clothesByType, ClothesType.BOTTOM);
    addBestClothes(selectedClothes, clothesByType, ClothesType.SHOES);

    // 조건부 아이템
    ScoredClothesDto weatherInfo = scoredClothes.get(0);
    if (weatherInfo != null) {
      if(shouldWearOuter(weatherInfo)) {
        addBestClothes(selectedClothes, clothesByType, ClothesType.OUTER);
      }
      if(shouldWearDress(weatherInfo)) {
        addBestClothes(selectedClothes, clothesByType, ClothesType.DRESS);
      }
    }

    // 랜덤 추가 TODO: 일단 50퍼 확률
    if (Math.random() < 0.5) {
      addOptionalClothes(selectedClothes, clothesByType);
    }

    return selectedClothes;
  }

  private void addBestClothes(List<ClothesDto> selectedClothes,
      Map<ClothesType, List<ScoredClothesDto>> clothesByType, ClothesType clothesType) {

    clothesByType.getOrDefault(clothesType, List.of())
        .stream()
        .findFirst()
        .ifPresent(clothes -> selectedClothes.add(convertToClothesDto(clothes)));
  }

  private void addOptionalClothes(List<ClothesDto> selectedClothes,
      Map<ClothesType, List<ScoredClothesDto>> clothesByType) {

    List<ClothesType> optionalTypes = List.of(
        ClothesType.ACCESSORY, ClothesType.BAG, ClothesType.HAT,
        ClothesType.SCARF, ClothesType.SOCKS, ClothesType.UNDERWEAR, ClothesType.ETC
    );

    for (ClothesType type : optionalTypes) {
      addBestClothes(selectedClothes, clothesByType, type);
    }
  }

  private boolean shouldWearOuter(ScoredClothesDto weatherInfo) {
    double feltTemperature = calculateFeltTemperature(weatherInfo);
    return feltTemperature < 20.0;
  }

  private boolean shouldWearDress(ScoredClothesDto weatherInfo) {
    double feltTemperature = calculateFeltTemperature(weatherInfo);
    return feltTemperature >= 20.0;
  }

  private double calculateFeltTemperature(ScoredClothesDto info) {
    if (info == null) {
      log.warn("ScoredClothesDto가 null입니다.");
      return 20.0; // 기본값
    }
    
    try {
      return info.temperatureCurrent()
          - (info.windSpeed() * 0.8)
          + (info.humidityCurrent() * 0.04)
          + (info.precipitationAmount() > 0 ? -2 : 0)
          + info.temperatureSensitivity();
    } catch (Exception e) {
      log.error("체감온도 계산 중 오류: {}", e.getMessage());
      return 20.0; // 기본값
    }
  }

  private ClothesDto convertToClothesDto(ScoredClothesDto clothes) {

    return ClothesDto.builder()
        .id(clothes.id())
        .ownerId(clothes.ownerId())
        .name(clothes.name())
        .type(clothes.type())
        .imageUrl(clothes.imageUrl())
        .attributes(createThicknessAttribute(clothes.thickness(), clothes.color()))
        .build();
  }

  private List<ClothesAttributeWithDefDto> createThicknessAttribute(String thickness, String color) {
    List<ClothesAttributeWithDefDto> attributes = new ArrayList<>();

    // 두께감 속성 추가
    if (thickness != null && !thickness.trim().isEmpty()) {
      attributeRepository.findByName("두께감")
          .ifPresent(attr -> attributes.add(ClothesAttributeWithDefDto.builder()
              .definitionId(attr.getId())
              .value(thickness)
              .definitionName(attr.getName())
              .selectableValues(attr.getDetails())
              .build()));
    }

    // 색상 속성 추가
    if (color != null && !color.trim().isEmpty()) {
      attributeRepository.findByName("색상")
          .ifPresent(attr -> attributes.add(ClothesAttributeWithDefDto.builder()
              .definitionId(attr.getId())
              .value(color)
              .definitionName(attr.getName())
              .selectableValues(attr.getDetails())
              .build()));
    }

    return attributes;
  }
}
