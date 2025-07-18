package com.example.ootd.domain.clothes.controller;

import com.example.ootd.config.api.AttributeApi;
import com.example.ootd.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeSearchCondition;
import com.example.ootd.domain.clothes.service.AttributeService;
import com.example.ootd.dto.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clothes/attribute-defs")
public class AttributeController implements AttributeApi {

  private final AttributeService attributeService;

  // 의상 속성 정의 목록 조회
  @GetMapping
  public ResponseEntity<PageResponse<ClothesAttributeDefDto>> find(
      @ModelAttribute @Valid ClothesAttributeSearchCondition condition) {

    log.info("의상 속성 정의 목록 조회 요청: {}", condition);

    PageResponse<ClothesAttributeDefDto> response = attributeService.findByCondition(condition);

    log.debug("의상 속성 정의 목록 조회 응답: attributeCount={}", response.data().size());

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(response);
  }

  // 의상 속성 정의 등록
  @PostMapping
  public ResponseEntity<ClothesAttributeDefDto> create(
      @RequestBody ClothesAttributeDefCreateRequest request) {

    log.info("의상 속성 정의 등록 요청: {}", request);

    ClothesAttributeDefDto response = attributeService.create(request);

    log.debug("의상 속성 정의 등록 응답: {}", response);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(response);
  }

  // 의상 속성 정의 삭제
  @DeleteMapping(path = "/{definitionId}")
  public ResponseEntity<Void> delete(@PathVariable UUID definitionId) {

    log.info("의상 속성 정의 삭제 요청: definitionId={}", definitionId);

    attributeService.delete(definitionId);

    log.debug("의상 속성 정의 삭제 응답");

    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }

  // 의상 속성 정의 수정
  @PatchMapping(path = "/{definitionId}")
  public ResponseEntity<ClothesAttributeDefDto> update(
      @PathVariable UUID definitionId,
      @RequestBody ClothesAttributeDefUpdateRequest request
  ) {

    log.info("의상 속성 정의 수정 요청: definitionId={}, request={}", definitionId, request);

    ClothesAttributeDefDto response = attributeService.update(request, definitionId);

    log.debug("의상 속성 정의 수정 응답: {}", request);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(response);
  }
}
