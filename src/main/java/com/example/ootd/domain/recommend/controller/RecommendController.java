package com.example.ootd.domain.recommend.controller;

import com.example.ootd.domain.llm.service.AIRecommendService;
import com.example.ootd.domain.recommend.dto.RecommendationDto;
import com.example.ootd.domain.recommend.service.RecommendService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
@Slf4j
public class RecommendController {

  private final RecommendService recommendService;
  private final AIRecommendService aiRecommendService;

  /**
   * 의상 추천
   * default: AI 추천
   * fail: 자체 알고리즘 추천
   */
  @GetMapping
  public ResponseEntity<RecommendationDto> recommend(@RequestParam("weatherId") UUID weatherId) {
    try {
      RecommendationDto recommendation = aiRecommendService.recommendClothes(weatherId);
      return ResponseEntity.ok(recommendation);
    } catch (Exception e) {
      log.error("AI 추천 실패, 기본 추천으로 fallback: {}", e.getMessage(), e);
      RecommendationDto recommendation = recommendService.recommend(weatherId);
      return ResponseEntity.ok(recommendation);
    }
  }
}
