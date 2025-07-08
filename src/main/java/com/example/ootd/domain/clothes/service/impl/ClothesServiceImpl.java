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
import java.util.HashSet;
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
  public ClothesDto create(ClothesCreateRequest request, MultipartFile image, UUID userId) {

    log.debug("의상 등록 시작: {}", request);

    Image clothesImage = imageService.upload(image);
    User user = userRepository.findById(userId)
        .orElseThrow(); // TODO: null 처리

    // TODO: 중복 속성 저장 예외처리
    
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
    updateAttributes(clothes, request.attributes());

    ClothesDto response = clothesMapper.toDto(clothes);

    log.info("의상 수정 완료: {}", response);

    return response;
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<ClothesDto> findByCondition(ClothesSearchCondition condition) {

    log.debug("의상 목록 조회 시작: {}", condition);

    List<Clothes> clothes = clothesRepository.findByCondition(condition);

    boolean hasNext = (clothes.size() > condition.limit());
    String nextCursor = null;
    UUID nextIdAfter = null;
    long totalCount = clothesRepository.countByCondition(condition.typeEqual(),
        condition.ownerId());

    // 다음 페이지 있는 경우
    if (hasNext) {
      clothes.remove(clothes.size() - 1);
      Clothes lastClothes = clothes.get(clothes.size() - 1);
      nextCursor = lastClothes.getCreatedAt().toString();
      nextIdAfter = lastClothes.getId();
    }

    PageResponse<ClothesDto> pageResponse = PageResponse.<ClothesDto>builder()
        .data(clothesMapper.toDto(clothes))
        .hasNext(hasNext)
        .nextCursor(nextCursor)
        .nextIdAfter(nextIdAfter)
        .sortBy("createdAt")
        .sortDirection("DESCENDING")
        .totalCount(totalCount)
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

  // 옷 등록 시 속성 초기화 - 
  private void setClothesAttributes(Clothes clothes, List<ClothesAttributeDto> attributeDtoList) {

    Map<UUID, Attribute> attributeMap = getAttributeMap(attributeDtoList);

    for (ClothesAttributeDto dto : attributeDtoList) {

      ClothesAttribute clothesAttribute = getClothesAttribute(dto, attributeMap, clothes);

      // ClothesAttribute 저장
      clothes.addClothesAttribute(clothesAttribute);
    }
  }

  // attributeDtoList를 이용해 AttributeMap 생성
  private Map<UUID, Attribute> getAttributeMap(List<ClothesAttributeDto> attributeDtoList) {

    // dto에서 attribute id만 분리
    List<UUID> clothesAttributeIdList = attributeDtoList.stream()
        .map(ClothesAttributeDto::definitionId)
        .toList();

    // attribute 정보 가져옴
    List<Attribute> attributeList = attributeRepository.findAllById(clothesAttributeIdList);

    // AttributeMap으로 변환하여 반환
    return attributeList.stream()
        .collect(Collectors.toMap(Attribute::getId, Function.identity()));
  }

  private ClothesAttribute getClothesAttribute(ClothesAttributeDto dto,
      Map<UUID, Attribute> attributeMap, Clothes clothes) {

    // 속성 정보 가져옴
    Attribute attribute = attributeMap.get(dto.definitionId());

    // 존재하지 않는 속성일 경우 예외처리
    if (attribute == null) {
      throw AttributeNotFoundException.withId(dto.definitionId());
    }
    // 해당 속성에 value(속성 내용)가 없는 경우 예외처리
    if (!attribute.getDetails().contains(dto.value())) {
      throw AttributeDetailNotFoundException.withValue(dto.value());
    }

    // ClothesAttribute 반환
    return ClothesAttribute.builder()
        .clothes(clothes)
        .attribute(attribute)
        .value(dto.value())
        .build();
  }

  // 옷 속성 수정
  public void updateAttributes(Clothes clothes, List<ClothesAttributeDto> dtoList) {

    // dtoList null일 경우 return
    if (dtoList == null || dtoList.isEmpty()) {
      return;
    }

    // dtoList를 Map으로 변환
    Map<UUID, ClothesAttributeDto> incomingDtoMap = dtoList.stream()
        .collect(Collectors.toMap(ClothesAttributeDto::definitionId, Function.identity()));

    // 요청 들어온 옷의 속성을 map으로 변환
    Map<UUID, ClothesAttribute> existingAttributes = clothes.getClothesAttributes().stream()
        .collect(Collectors.toMap(attr -> attr.getAttribute().getId(), Function.identity()));

    // incomingDtoMap과 existingAttributes에 존재하는 id를 모드 allIds에 저장(중복 제외)
    Set<UUID> allIds = new HashSet<>();
    allIds.addAll(existingAttributes.keySet());
    allIds.addAll(incomingDtoMap.keySet());

    for (UUID id : allIds) {
      ClothesAttribute existing = existingAttributes.get(id); // 옷에 저장되어 있는 속성
      ClothesAttributeDto incomingDto = incomingDtoMap.get(id); // 요청 들어온 속성

      if (existing != null && incomingDto == null) {
        // 삭제
        // 옷에 저장되어 있고 요청엔 없는 경우 삭제
        clothes.removeClothesAttribute(existing);

      } else if (existing != null) {
        // 수정
        // 옷에 저장되어 있고, 요청에도 있는 경우
        if (!existing.getValue().equals(incomingDto.value())) { // 둘의 value가 같지 않으면 value 수정
          validateValue(existing.getAttribute(), incomingDto.value());
          existing.updateValue(incomingDto.value());
        }

      } else {
        // 추가
        // 옷에 없고, 요청에 있는 경우
        Attribute attribute = attributeRepository.findById(id)  // 속성이 존재하지 않는 값일 경우 예외처리
            .orElseThrow(() -> AttributeNotFoundException.withId(id));
        validateValue(attribute, incomingDto.value());

        // 새로운 ClothesAttribute 만들어서 저장
        ClothesAttribute newAttr = ClothesAttribute.builder()
            .clothes(clothes)
            .attribute(attribute)
            .value(incomingDto.value())
            .build();
        clothes.addClothesAttribute(newAttr);
      }
    }
  }

  // 속성에 존재하지 않는 값일 경우 예외처리
  private void validateValue(Attribute attribute, String value) {
    if (!attribute.isValidValue(value)) {
      throw AttributeDetailNotFoundException.withValue(value);
    }
  }
}
