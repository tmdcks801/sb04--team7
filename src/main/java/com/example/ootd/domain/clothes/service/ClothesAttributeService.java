package com.example.ootd.domain.clothes.service;

import com.example.ootd.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeSearchRequest;
import com.example.ootd.dto.PageResponse;
import java.util.UUID;

/**
 * 의상 속성 정의 관리
 */
public interface ClothesAttributeService {

  // 의상 속성 정의 등록
  ClothesAttributeDefDto create(ClothesAttributeDefCreateRequest request);

  // 의상 속성 정의 수정
  ClothesAttributeDefDto update(ClothesAttributeDefUpdateRequest request, UUID definitionId);

  // 의상 속성 정의 목록 조회
  PageResponse<ClothesAttributeDefDto> findByCondition(ClothesAttributeSearchRequest request);

  // 의상 속성 정의 삭제
  void delete(UUID definitionId);
}
