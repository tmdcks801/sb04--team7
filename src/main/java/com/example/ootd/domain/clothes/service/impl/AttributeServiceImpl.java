package com.example.ootd.domain.clothes.service.impl;

import com.example.ootd.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeSearchCondition;
import com.example.ootd.domain.clothes.entity.Attribute;
import com.example.ootd.domain.clothes.mapper.AttributeMapper;
import com.example.ootd.domain.clothes.repository.AttributeRepository;
import com.example.ootd.domain.clothes.service.AttributeService;
import com.example.ootd.dto.PageResponse;
import com.example.ootd.exception.clothes.AttributeNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AttributeServiceImpl implements AttributeService {

  private final AttributeRepository attributeRepository;
  private final AttributeMapper attributeMapper;

  @Override
  public ClothesAttributeDefDto create(ClothesAttributeDefCreateRequest request) {

    log.debug("의상 속성 정의 등록 시작: {}", request);

    Attribute attribute = Attribute.builder()
        .name(request.name())
        .details(request.selectableValue())
        .build();

    attributeRepository.save(attribute);

    ClothesAttributeDefDto clothesAttributeDefDto = attributeMapper.toDto(attribute);

    log.info("의상 속성 정의 등록 완료: attributeId={}, attributeName={}", attribute.getId(),
        attribute.getName());

    return clothesAttributeDefDto;
  }

  @Override
  public ClothesAttributeDefDto update(ClothesAttributeDefUpdateRequest request,
      UUID definitionId) {

    log.debug("의상 속성 정의 수정 시작: definitionId={}, request={}", definitionId, request);

    Attribute attribute = getAttributeById(definitionId);

    updateName(attribute, request.name());
    updateDetails(attribute, request.selectableValue());

    ClothesAttributeDefDto clothesAttributeDefDto = attributeMapper.toDto(attribute);

    log.info("의상 속성 정의 수정 완료: definitionId={}, name={}, details={}", attribute.getId(),
        attribute.getName(), attribute.getDetails());

    return clothesAttributeDefDto;
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<ClothesAttributeDefDto> findByCondition(
      ClothesAttributeSearchCondition condition) {

    log.debug("의상 속성 정의 조회 시작: {}", condition);

    List<Attribute> attributes = attributeRepository.findByCondition(condition);

    // 다음 페이지 없는 경우
    if (attributes.size() <= condition.limit()) {

      PageResponse<ClothesAttributeDefDto> pageResponse = PageResponse.<ClothesAttributeDefDto>builder()
          .data(attributeMapper.toDtoList(attributes))
          .hasNext(false)
          .nextCursor(null)
          .nextIdAfter(null)
          .sortBy(condition.sortBy())
          .sortDirection(condition.sortDirection())
          .totalCount(attributeRepository.countByKeyword(condition.keywordLike()))
          .build();

      log.info("의상 속성 정의 조회 완료: dataCount={}", attributes.size());

      return pageResponse;
    }

    // 다음 페이지 있는 경우
    attributes.remove(attributes.size() - 1); // 다음 페이지 확인용 마지막 요소 삭제
    Attribute lastAttribute = attributes.get(attributes.size() - 1);  // 페이지 마지막 속성
    String nextCursor = setNextCursor(lastAttribute, condition.sortBy());
    UUID nextIdAfter = lastAttribute.getId();

    PageResponse<ClothesAttributeDefDto> pageResponse = PageResponse.<ClothesAttributeDefDto>builder()
        .data(attributeMapper.toDtoList(attributes))
        .hasNext(true)
        .nextCursor(nextCursor)
        .nextIdAfter(nextIdAfter)
        .sortBy(condition.sortBy())
        .sortDirection(condition.sortDirection())
        .totalCount(attributeRepository.countByKeyword(condition.keywordLike()))
        .build();

    log.info("의상 속성 정의 조회 완료: dataCount={}", attributes.size());

    return pageResponse;
  }

  @Override
  public void delete(UUID definitionId) {

    log.debug("의상 속성 정의 삭제 시작: definitionId={}", definitionId);

    Attribute attribute = getAttributeById(definitionId);
    attributeRepository.delete(attribute);

    log.info("의상 속성 정의 삭제 완료");
  }


  private Attribute getAttributeById(UUID attributeId) {

    return attributeRepository.findById(attributeId)
        .orElseThrow(() -> AttributeNotFoundException.withId(attributeId));
  }

  private void updateName(Attribute attribute, String updateName) {

    if (!attribute.getName().equals(updateName)) {
      attribute.updateName(updateName);
    }
  }

  private void updateDetails(Attribute attribute, List<String> selectableValue) {

    if (!new HashSet<>(attribute.getDetails()).equals(new HashSet<>(selectableValue))) {
      attribute.updateDetails(selectableValue);
    }
  }

  private String setNextCursor(Attribute lastAttribute, String sortBy) {

    switch (sortBy) {
      case "name":
        return lastAttribute.getName();
      case "createdAt":
        return lastAttribute.getCreatedAt().toString();
      default:
        return null;
    }
  }
}
