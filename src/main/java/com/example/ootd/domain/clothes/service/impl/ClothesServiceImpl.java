package com.example.ootd.domain.clothes.service.impl;

import com.example.ootd.domain.clothes.dto.data.ClothesAttributeDto;
import com.example.ootd.domain.clothes.dto.data.ClothesDto;
import com.example.ootd.domain.clothes.dto.request.ClothesCreateRequest;
import com.example.ootd.domain.clothes.dto.request.ClothesSearchCondition;
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
import com.example.ootd.exception.clothes.AttributeDetailNotFoundException;
import com.example.ootd.exception.clothes.AttributeNotFoundException;
import com.example.ootd.exception.clothes.ClothesNotFountException;
import com.querydsl.core.util.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
  public PageResponse<ClothesDto> findByCondition(ClothesSearchCondition condition) {

    log.debug("의상 목록 조회 시작: {}", condition);

    List<Clothes> clothes = clothesRepository.findByCondition(condition);

    // 다음 페이지 없는 경우
    if (clothes.size() <= condition.limit()) {

      PageResponse<ClothesDto> pageResponse = PageResponse.<ClothesDto>builder()
          .data(clothesMapper.toDtoList(clothes))
          .hasNext(false)
          .nextCursor(null)
          .nextIdAfter(null)
          .sortBy("createdAt")
          .sortDirection("DESCENDING")
          .totalCount(
              clothesRepository.countByCondition(condition.typeEqual(), condition.ownerId()))
          .build();

      log.info("의상 목록 조회 완료: dataCount={}", clothes.size());

      return pageResponse;
    }

    // 다음 페이지 있는 경우
    clothes.remove(clothes.size() - 1); // 다음 페이지 확인용 요소 삭제
    Clothes lastClothes = clothes.get(clothes.size() - 1);
    String nextCursor = lastClothes.getCreatedAt().toString();
    UUID nextIdAfter = lastClothes.getId();

    PageResponse<ClothesDto> pageResponse = PageResponse.<ClothesDto>builder()
        .data(clothesMapper.toDtoList(clothes))
        .hasNext(true)
        .nextCursor(nextCursor)
        .nextIdAfter(nextIdAfter)
        .sortBy("createdAt")
        .sortDirection("DESCENDING")
        .totalCount(
            clothesRepository.countByCondition(condition.typeEqual(), condition.ownerId()))
        .build();

    log.info("의상 목록 조회 완료: dataCount={}", clothes.size());

    return pageResponse;
  }

  @Override
  public void delete(UUID clothesId) {

    log.debug("의상 삭제 시작: clothesId={}", clothesId);

    Clothes clothes = getClothesById(clothesId);
    clothesRepository.delete(clothes);

    log.info("의상 삭제 완료");
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

  // TODO: 테스트 후 수정
  private void updateAttribute(Clothes clothes, List<ClothesAttributeDto> attributeDtoList) {

    if (attributeDtoList == null) {
      return;
    }

    // 1. Map<attributeId, value> 형태로 요청 데이터를 정리
    Map<UUID, String> incomingAttrMap = attributeDtoList.stream()
        .collect(Collectors.toMap(
            ClothesAttributeDto::definitionId,
            ClothesAttributeDto::value,
            (v1, v2) -> v2 // 중복 키가 있을 경우 마지막 키 사용 (중복 방지)
        ));

    // 2. 기존 속성 리스트
    Set<ClothesAttribute> currentAttributes = clothes.getClothesAttributes();

    // 3. 삭제 대상: 요청에 없는 attributeId → 제거
    currentAttributes.removeIf(existing ->
        !incomingAttrMap.containsKey(existing.getAttribute().getId())
    );

    Map<UUID, Attribute> attributeMap = getAttributeMap(attributeDtoList);

    // 4. 새로 추가할 속성만 Clothes에 추가
    for (Map.Entry<UUID, String> entry : incomingAttrMap.entrySet()) {
      UUID attributeId = entry.getKey();
      String newValue = entry.getValue();

      // 기존 속성 찾기
      ClothesAttribute existing = currentAttributes.stream()
          .filter(attr -> attr.getAttribute().getId().equals(attributeId))
          .findFirst()
          .orElse(null);

      if (existing != null) {
        // value가 다르면 업데이트
        if (!existing.getValue().equals(newValue)) {
          existing.updateValue(newValue);
        }
      } else {
        Attribute attribute = attributeMap.get(attributeId);
        ClothesAttribute newAttr = new ClothesAttribute(clothes, attribute, newValue);
        clothes.addClothesAttribute(newAttr);
      }
    }
  }


  private void setClothesAttributes(Clothes clothes, List<ClothesAttributeDto> attributeDtoList) {

    Map<UUID, Attribute> attributeMap = getAttributeMap(attributeDtoList);

    for (ClothesAttributeDto dto : attributeDtoList) {

      ClothesAttribute clothesAttribute = getClothesAttribute(dto, attributeMap, clothes);

      clothes.addClothesAttribute(clothesAttribute);
    }
  }

  private Map<UUID, Attribute> getAttributeMap(List<ClothesAttributeDto> attributeDtoList) {

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
    // 해당 속성에 value(속성 내용)가 없는 경우 예외처리
    if (!attribute.getDetails().contains(dto.value())) {
      throw AttributeDetailNotFoundException.withValue(dto.value());
    }

    return ClothesAttribute.builder()
        .clothes(clothes)
        .attribute(attribute)
        .value(dto.value())
        .build();
  }
}
