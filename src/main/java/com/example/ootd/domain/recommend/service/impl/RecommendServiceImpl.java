package com.example.ootd.domain.recommend.service.impl;

import com.example.ootd.domain.clothes.dto.data.ClothesDto;
import com.example.ootd.domain.clothes.dto.data.RecommendationDto;
import com.example.ootd.domain.clothes.dto.data.ScoredClothesDto;
import com.example.ootd.domain.clothes.entity.ClothesType;
import com.example.ootd.domain.recommend.service.ClothesCalculator;
import com.example.ootd.domain.recommend.service.ClothesSelector;
import com.example.ootd.domain.recommend.repository.RecommendQueryRepository;
import com.example.ootd.domain.recommend.service.RecommendService;
import com.example.ootd.security.CustomUserDetails;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RecommendServiceImpl implements RecommendService {

  private final RecommendQueryRepository recommendQueryRepository;
  private final ClothesCalculator scoreCalculator;
  private final ClothesSelector clothesSelector;
  private final CacheManager cacheManager;

  @Cacheable(value = "recommendations",
      key = "#userId + '-' + T(java.time.LocalDateTime).now()")
  @Override
  public RecommendationDto recommend(UUID weatherId) {
    log.info("옷 추천 요청: weatherId={}", weatherId);

    UUID userId = getCurrentUserId();

    // 데이터 조회
    List<Object[]> queryResults = recommendQueryRepository
        .findClothesRecommendations(weatherId, userId);

    if (queryResults.isEmpty()) {
      log.debug("추천 가능한 의상이 없음: userId={}, weatherId={}", userId, weatherId);
      return createEmptyRecommendation(weatherId, userId);
    }

    // 점수 재계산 (기존 쿼리 결과의 점수 대신 새로운 계산기 사용)
    List<ScoredClothesDto> clothesWithWeather = queryResults.stream()
        .map(this::convertToScoredClothes)
        .toList();

    // 의상 선택
    List<ScoredClothesDto> scoredClothes = clothesWithWeather.stream()
        .map(this::calculateScore)
        .sorted((a, b) -> Double.compare(b.score(), a.score()))
        .toList();

    List<ClothesDto> recommendedClothes = clothesSelector.selectRecommendedClothes(scoredClothes);

    return RecommendationDto.builder()
        .weatherId(weatherId)
        .userId(userId)
        .clothes(recommendedClothes)
        .build();
  }

  private ScoredClothesDto calculateScore(ScoredClothesDto clothes) {
    double calculateScore = scoreCalculator.calculateScore(clothes);

    return ScoredClothesDto.builder()
        .id(clothes.id())
        .name(clothes.name())
        .type(clothes.type())
        .imageUrl(clothes.imageUrl())
        .temperatureCurrent(clothes.temperatureCurrent())
        .precipitationAmount(clothes.precipitationAmount())
        .humidityCurrent(clothes.humidityCurrent())
        .windSpeed(clothes.windSpeed())
        .temperatureSensitivity(clothes.temperatureSensitivity())
        .thickness(clothes.thickness())
        .color(clothes.color())
        .score(calculateScore) // 새로 계산된 점수
        .build();
  }

  private RecommendationDto createEmptyRecommendation(UUID weatherId, UUID userId) {
    return RecommendationDto.builder()
        .weatherId(weatherId)
        .userId(userId)
        .clothes(List.of())
        .build();
  }

  // 사용자 옷 추가/삭제시 호출
  public void evictUserCache(UUID userId) {
    Cache cache = cacheManager.getCache("recommendations");
    if (cache != null) {
      LocalDate today = LocalDate.now();
      LocalDate yesterday = today.minusDays(1);

      cache.evict(userId + "_" + today);
      cache.evict(userId + "_" + yesterday);
    }
  }

  @Override
  public void safeEvictUserCache(UUID userId) {
    try {
      this.evictUserCache(userId);
    } catch (Exception e) {
      log.warn("캐시 삭제 실패 (데이터 변경은 완료됨): userId={}", userId, e);
    }
  }

  /**
   * Object[] 쿼리 결과를 ScoredClothesDto로 변환 (기존 점수는 무시)
   */
  private ScoredClothesDto convertToScoredClothes(Object[] row) {
    return ScoredClothesDto.builder()
        .id(UUID.fromString(row[0].toString()))
        .name(row[1].toString())
        .type(ClothesType.valueOf(row[2].toString()))
        .imageUrl(row[3] != null ? row[3].toString() : null)
        .temperatureCurrent(((Number) row[4]).doubleValue())
        .precipitationAmount(((Number) row[5]).doubleValue())
        .humidityCurrent(((Number) row[6]).doubleValue())
        .windSpeed(((Number) row[7]).doubleValue())
        .temperatureSensitivity(((Number) row[8]).intValue())
        .thickness(row[9] != null ? row[9].toString() : null)
        .color(row[10] != null ? row[10].toString() : null)
        .score(0.0) // 임시 점수 -> 어차피 변경됨
        .build();
  }

  /**
   * 현재 인증된 사용자 ID 조회
   */
  private UUID getCurrentUserId() {
    CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
        .getAuthentication().getPrincipal();
    return userDetails.getUser().getId();
  }
}
