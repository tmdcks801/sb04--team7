package com.example.ootd.domain.llm.service;

import com.example.ootd.domain.clothes.entity.Clothes;
import com.example.ootd.domain.clothes.repository.ClothesRepository;
import com.example.ootd.domain.recommend.dto.RecommendClothesDto;
import com.example.ootd.domain.recommend.dto.RecommendationDto;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.domain.weather.entity.Weather;
import com.example.ootd.domain.weather.repository.WeatherRepository;
import com.example.ootd.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIRecommendService {

  private final OpenAIService openAIService;
  private final WeatherRepository weatherRepository;
  private final UserRepository userRepository;
  private final ClothesRepository clothesRepository;
  private final StringRedisTemplate stringRedisTemplate;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public RecommendationDto recommendClothes(UUID weatherId) throws Exception {

    UUID userId = getCurrentUserId();
    
    // 1. 먼저 배치로 미리 생성된 캐시 확인
    String batchCacheKey = "aiRecommendation::" + weatherId + ":" + userId;
    
    try {
      log.info("캐시 조회 시도: key={}", batchCacheKey);
      
      String cachedJson = stringRedisTemplate.opsForValue().get(batchCacheKey);
      log.info("캐시 조회 결과: json={}", cachedJson != null ? "존재(" + cachedJson.length() + "자)" : "null");
      
      if (cachedJson != null) {
        RecommendationDto cached = objectMapper.readValue(cachedJson, RecommendationDto.class);
        log.info("배치 캐시 히트: userId={}, weatherId={}", userId, weatherId);
        return cached;
      } else {
        log.warn("캐시 미스: key={} 데이터 없음", batchCacheKey);
      }
    } catch (Exception e) {
      log.warn("캐시 조회 실패: key={}, error={}", batchCacheKey, e.getMessage());
      // 캐시 조회 실패 시 계속 진행
    }
    
    // 2. 캐시 미스 - 실시간 AI 호출 후 캐시에 저장
    log.warn("캐시 미스 발생 - 실시간 AI 호출: userId={}, weatherId={}", userId, weatherId);
    RecommendationDto result = performAIRecommendation(weatherId, userId);
    
    // 실시간 호출 결과도 캐시에 저장 (JSON 문자열로 저장, 24시간 TTL)
    try {
      String resultJson = objectMapper.writeValueAsString(result);
      stringRedisTemplate.opsForValue().set(batchCacheKey, resultJson, Duration.ofHours(24));
      log.info("캐시 저장 성공: userId={}, weatherId={}", userId, weatherId);
    } catch (Exception e) {
      log.warn("캐시 저장 실패: {}", e.getMessage());
      // 캐시 저장 실패해도 결과는 반환
    }
    
    return result;
  }

  public RecommendationDto recommendClothesForPreload(UUID weatherId, UUID userId) throws Exception {
    return performAIRecommendation(weatherId, userId);
  }

  // 실제 AI 추천 로직
  private RecommendationDto performAIRecommendation(UUID weatherId, UUID userId) throws Exception {

    // 1. 데이터 조회
    Weather weather = weatherRepository.findById(weatherId).orElseThrow();
    User user = userRepository.findById(userId).orElseThrow();
    List<Clothes> clothesList = clothesRepository.findByUserId(user.getId());

    // 2. 옷 정보 단순화
    List<Map<String, Object>> simplifiedClothes = clothesList.stream()
        .map(ClothesSimplifier::simplify)
        .collect(Collectors.toList());

    // 3. LLM 입력용 JSON 구성
    Map<String, Object> llmInput = Map.of(
        "user", Map.of("id", user.getId().toString()),
        "weather", Map.of(
            "id", weather.getId().toString(),
            "temperature", weather.getTemperature(),
            "humidity", weather.getHumidity(),
            "precipitation", weather.getPrecipitation(),
            "wind", weather.getWindSpeed(),
            "status", weather.getSkyStatus()
        ),
        "clothes", simplifiedClothes
    );

    // 4. GPT 호출
    String recommendationJson = openAIService.getRecommendation(llmInput);

    // 5. 결과 JSON 문자열을 RecommendClothesDto 리스트로 파싱
    ObjectMapper mapper = new ObjectMapper();
    
    // OpenAI 응답에서 JSON 부분만 추출 (설명 텍스트 제거)
    String jsonPart = extractJsonFromResponse(recommendationJson);
    Map<String, Object> result = mapper.readValue(jsonPart, Map.class);
    
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> clothesData = (List<Map<String, Object>>) result.get("clothes");
    
    if (clothesData == null) {
      log.warn("AI 응답에서 clothes 데이터를 찾을 수 없습니다. 응답: {}", recommendationJson);
      return RecommendationDto.builder()
          .weatherId(weatherId)
          .userId(userId)
          .clothes(List.of())
          .build();
    }
    
    List<RecommendClothesDto> recommendedClothes = clothesData.stream()
        .map(clothesMap -> {
          try {
            return convertToRecommendClothesDto(clothesMap);
          } catch (Exception e) {
            log.warn("의상 데이터 변환 실패: {}", clothesMap, e);
            return null;
          }
        })
        .filter(dto -> dto != null)
        .collect(Collectors.toList());

    return RecommendationDto.builder()
        .weatherId(weatherId)
        .userId(userId)
        .clothes(recommendedClothes)
        .build();
  }

  private RecommendClothesDto convertToRecommendClothesDto(Map<String, Object> clothesMap) {
    return RecommendClothesDto.builder()
        .clothesId(UUID.fromString(clothesMap.get("clothesId").toString()))
        .name(clothesMap.get("name").toString())
        .imageUrl(clothesMap.get("imageUrl") != null ? clothesMap.get("imageUrl").toString() : null)
        .type(com.example.ootd.domain.clothes.entity.ClothesType.valueOf(clothesMap.get("type").toString()))
        .attributes(convertAttributes(clothesMap.get("attributes")))
        .build();
  }

  @SuppressWarnings("unchecked")
  private List<com.example.ootd.domain.clothes.dto.data.ClothesAttributeWithDefDto> convertAttributes(Object attributesObj) {
    if (attributesObj == null) {
      return List.of();
    }
    
    List<Map<String, Object>> attributesList = (List<Map<String, Object>>) attributesObj;
    return attributesList.stream()
        .map(attr -> com.example.ootd.domain.clothes.dto.data.ClothesAttributeWithDefDto.builder()
            .definitionId(UUID.fromString(attr.get("definitionId").toString()))
            .definitionName(attr.get("definitionName").toString())
            .selectableValues((List<String>) attr.get("selectableValues"))
            .value(attr.get("value").toString())
            .build())
        .collect(Collectors.toList());
  }

  /**
   * AI 응답에서 JSON 부분만 추출 (설명 텍스트 제거)
   */
  private String extractJsonFromResponse(String response) {
    int startIndex = response.indexOf("{");
    int endIndex = response.lastIndexOf("}");
    
    if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
      return response.substring(startIndex, endIndex + 1);
    }
    
    // JSON을 찾지 못하면 전체 응답 반환
    return response;
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
