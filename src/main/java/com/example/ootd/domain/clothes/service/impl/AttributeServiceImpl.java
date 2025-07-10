package com.example.ootd.domain.clothes.service.impl;

import com.example.ootd.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeSearchCondition;
import com.example.ootd.domain.clothes.entity.Attribute;
import com.example.ootd.domain.clothes.mapper.AttributeMapper;
import com.example.ootd.domain.clothes.repository.AttributeRepository;
import com.example.ootd.domain.clothes.service.AttributeService;
import com.example.ootd.domain.notification.dto.NotificationEvent;
import com.example.ootd.domain.notification.enums.NotificationLevel;
import com.example.ootd.domain.notification.service.inter.NotificationPublisherInterface;
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
  private final NotificationPublisherInterface notificationPublisher;

  @Override
  public ClothesAttributeDefDto create(ClothesAttributeDefCreateRequest request) {

    log.debug("의상 속성 정의 등록 시작: {}", request);

    Attribute attribute = Attribute.builder()
        .name(request.name())
        .details(request.selectableValues())
        .build();

    attributeRepository.save(attribute);

    // 모든 사용자에게 알림
    notificationPublisher.publishToAll(
        new NotificationEvent("새로운 의상 속성이 등록되었어요.", "내 의상에 [" + attribute.getName() + "] 속성을 추가해보세요.",
            NotificationLevel.INFO)
    );

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
    updateDetails(attribute, request.selectableValues());

    // 모든 사용자에게 알림
    notificationPublisher.publishToAll(
        new NotificationEvent("의상 속성이 변경되었어요.", "[" + attribute.getName() + "] 속성을 확인해보세요.",
            NotificationLevel.INFO)
    );

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

    boolean hasNext = (attributes.size() > condition.limit());
    String nextCursor = null;
    UUID nextIdAfter = null;
    long totalCount = attributeRepository.countByKeyword(condition.keywordLike());

    // 다음 페이지 있는 경우
    if (hasNext) {
      attributes.remove(attributes.size() - 1);   // 다음 페이지 확인용 마지막 요소 삭제
      Attribute lastAttribute = attributes.get(attributes.size() - 1);
      nextCursor = setNextCursor(lastAttribute, condition.sortBy());
      nextIdAfter = lastAttribute.getId();
    }

    PageResponse<ClothesAttributeDefDto> response = PageResponse.<ClothesAttributeDefDto>builder()
        .data(attributeMapper.toDtoList(attributes))
        .hasNext(hasNext)
        .nextCursor(nextCursor)
        .nextIdAfter(nextIdAfter)
        .sortBy(condition.sortBy())
        .sortDirection(condition.sortDirection())
        .totalCount(totalCount)
        .build();

    log.info("의상 속성 정의 조회 완료: dataCount={}", attributes.size());

    return response;
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

  private void updateDetails(Attribute attribute, List<String> selectableValues) {

    if (!new HashSet<>(attribute.getDetails()).equals(new HashSet<>(selectableValues))) {
      attribute.updateDetails(selectableValues);
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
