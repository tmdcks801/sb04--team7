package com.example.ootd.domain.clothes.service.impl;

import com.example.ootd.domain.clothes.dto.data.ClothesAttributeDto;
import com.example.ootd.domain.clothes.dto.data.ClothesDto;
import com.example.ootd.domain.clothes.dto.data.RecommendationDto;
import com.example.ootd.domain.clothes.dto.request.ClothesCreateRequest;
import com.example.ootd.domain.clothes.dto.request.ClothesSearchRequest;
import com.example.ootd.domain.clothes.dto.request.ClothesUpdateRequest;
import com.example.ootd.domain.clothes.entity.Attribute;
import com.example.ootd.domain.clothes.entity.Clothes;
import com.example.ootd.domain.clothes.entity.ClothesAttribute;
import com.example.ootd.domain.clothes.entity.ClothesType;
import com.example.ootd.domain.clothes.mapper.ClothesMapper;
import com.example.ootd.domain.clothes.repository.AttributeRepository;
import com.example.ootd.domain.clothes.repository.ClothesRepository;
import com.example.ootd.domain.clothes.service.ClothesService;
import com.example.ootd.domain.image.entity.Image;
import com.example.ootd.domain.image.service.ImageService;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.dto.PageResponse;
import com.example.ootd.exception.clothes.AttributeNotFoundException;
import com.example.ootd.exception.clothes.ClothesNotFountException;
import com.querydsl.core.util.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClothesServiceImpl implements ClothesService {

  private final ImageService imageService;
  private final ClothesRepository clothesRepository;
  private final UserRepository userRepository;
  private final AttributeRepository attributeRepository;
  private final ClothesMapper clothesMapper;

  @Override
  public ClothesDto create(ClothesCreateRequest request, MultipartFile image) {

    log.debug("의상 등록 시작: {}", request);

    Image clothesImage = imageService.upload(image);
    User user = userRepository.findById(request.ownerId())
        .orElseThrow(); // TODO: null 처리

    // Clothes 등록
    Clothes clothes = Clothes.builder()
        .image(clothesImage)
        .user(user)
        .name(request.name())
        .type(request.type())
        .build();

    // ClothesAttribute 등록 및 Clothes.clothesAttributes에 추가
    setClothesAttributes(clothes, request.attributes());

    clothesRepository.save(clothes);

    ClothesDto response = clothesMapper.toDto(clothes);

    log.info("의상 등록 완료: {}", response);

    return response;
  }

  @Override
  public ClothesDto update(ClothesUpdateRequest request, MultipartFile image, UUID clothesId) {

    log.debug("의상 수정 시작: {}", request);

    Clothes clothes = getClothesById(clothesId);

    updateName(clothes, request.name());
    updateType(clothes, request.type());
    updateImage(clothes, image);
    updateAttribute(clothes, request.attributes());

    ClothesDto response = clothesMapper.toDto(clothes);

    log.info("의상 수정 완료: {}", response);

    return response;
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<ClothesDto> findByCondition(ClothesSearchRequest request) {
    return null;
  }

  @Override
  public void delete(UUID clothesId) {

    log.debug("의상 삭제 시작: clothesId={}", clothesId);

    Clothes clothes = getClothesById(clothesId);
    clothesRepository.delete(clothes);

    log.info("의상 삭제 완료");
  }

  @Override
  @Transactional(readOnly = true)
  public RecommendationDto recommend(UUID weatherId) {
    return null;
  }

  private Clothes getClothesById(UUID clothesId) {
    return clothesRepository.findById(clothesId)
        .orElseThrow(() -> ClothesNotFountException.withId(clothesId));
  }

  private void updateName(Clothes clothes, String name) {
    if (!StringUtils.isNullOrEmpty(name)) {
      clothes.updateName(name);
    }
  }

  private void updateType(Clothes clothes, ClothesType type) {
    if (type != null) {
      clothes.updateType(type);
    }
  }

  private void updateImage(Clothes clothes, MultipartFile image) {
    if (image != null && !image.isEmpty()) {
      Image clothesImage = imageService.upload(image);
      clothes.updateImage(clothesImage);
    }
  }

  private void updateAttribute(Clothes clothes, List<ClothesAttributeDto> attributeDtoList) {

    Map<UUID, Attribute> attributeMap = getAttributeMap(clothes, attributeDtoList);

    for (ClothesAttributeDto dto : attributeDtoList) {

      ClothesAttribute clothesAttribute = getClothesAttribute(dto, attributeMap, clothes);

      if (clothes.getClothesAttributes().contains(clothesAttribute)) {
        clothes.removeClothesAttribute(clothesAttribute);
      } else {
        clothes.addClothesAttribute(clothesAttribute);
      }
    }
  }

  private void setClothesAttributes(Clothes clothes, List<ClothesAttributeDto> attributeDtoList) {

    Map<UUID, Attribute> attributeMap = getAttributeMap(clothes, attributeDtoList);

    for (ClothesAttributeDto dto : attributeDtoList) {

      ClothesAttribute clothesAttribute = getClothesAttribute(dto, attributeMap, clothes);

      clothes.addClothesAttribute(clothesAttribute);
    }
  }

  private Map<UUID, Attribute> getAttributeMap(Clothes clothes,
      List<ClothesAttributeDto> attributeDtoList) {
    List<UUID> clothesAttributeIdList = attributeDtoList.stream()
        .map(ClothesAttributeDto::definitionId)
        .toList();

    List<Attribute> attributeList = attributeRepository.findAllById(clothesAttributeIdList);

    return attributeList.stream()
        .collect(Collectors.toMap(Attribute::getId, Function.identity()));
  }

  private ClothesAttribute getClothesAttribute(ClothesAttributeDto dto,
      Map<UUID, Attribute> attributeMap, Clothes clothes) {

    Attribute attribute = attributeMap.get(dto.definitionId());
    if (attribute == null) {
      throw AttributeNotFoundException.withId(dto.definitionId());
    }

    return ClothesAttribute.builder()
        .clothes(clothes)
        .attribute(attribute)
        .value(dto.value())
        .build();
  }
}
