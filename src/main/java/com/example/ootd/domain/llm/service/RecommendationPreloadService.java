package com.example.ootd.domain.llm.service;

import com.example.ootd.domain.recommend.dto.RecommendationDto;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.domain.weather.entity.Weather;
import com.example.ootd.domain.weather.repository.WeatherRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationPreloadService {

  private final UserRepository userRepository;
  private final WeatherRepository weatherRepository;
  private final AIRecommendService aiRecommendService;
  private final StringRedisTemplate stringRedisTemplate;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Async("preloadTaskExecutor")
  @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Seoul") // 매일 오전 6시
  public void preloadMorningRecommendations() {
    log.info("의상 추천 배치 시작");

    long startTime = System.currentTimeMillis();
    int successCount = 0;
    int totalUsers = 0;
    int usersWithLocation = 0;

    try {
      // 전체 날씨 데이터 개수 확인
      List<Weather> allTodayWeathers = weatherRepository.findAll().stream()
          .filter(w -> w.getForecastAt().toLocalDate().equals(LocalDate.now()))
          .collect(Collectors.toList());
      log.info("오늘자 전체 날씨 데이터 개수: {}, 지역: {}", 
          allTodayWeathers.size(),
          allTodayWeathers.stream().map(Weather::getRegionName).distinct().collect(Collectors.toList()));

      List<User> users = userRepository.findAll(); // TODO:로그인 기록이 있으면 좋은데 없으니 일단 모든 유저
      totalUsers = users.size();

      for(User user : users) {
        if (user.getLocation() != null) {
          usersWithLocation++;
        }
        
        try{
          preloadUserRecommendations(user);
          successCount++;
        } catch (Exception e) {
          log.warn("사용자 추천 배치 실패 : user = {}, error = {}", user.getId(), e.getMessage());
        }
      }
      long duration = System.currentTimeMillis() - startTime;
      log.info("의상 추천 배치 완료: 총 {}명, location 있는 사용자 {}명, 성공 {}명, 소요시간 {}ms",
          totalUsers, usersWithLocation, successCount, duration);
    } catch (Exception e) {
      log.error("의상 추천 배치 전체 실패", e);
    }
  }

  private void preloadUserRecommendations(User user) throws Exception {
    // 사용자의 location 확인
    if (user.getLocation() == null) {
      log.warn("사용자 {}의 location 정보가 없습니다.", user.getId());
      return;
    }
    
    log.info("사용자 {} location: {}", user.getId(), user.getLocation().getLocationNames());
    
    List<Weather> todayWeathers = weatherRepository.findTodayWeatherByUserLocation(user.getId());
    log.info("사용자 {}의 오늘 날씨 데이터 개수: {}", user.getId(), todayWeathers.size());

    for (Weather weather : todayWeathers) {
      try {
        // AI 추천 받기 - LLM 호출
        RecommendationDto recommendation = aiRecommendService.recommendClothesForPreload(weather.getId(), user.getId());

        // Redis에 JSON 문자열로 저장
        String cacheKey = generateBatchCacheKey(weather.getId(), user.getId());
        String recommendationJson = objectMapper.writeValueAsString(recommendation);
        stringRedisTemplate.opsForValue().set(cacheKey, recommendationJson, Duration.ofHours(24));
        log.info("배치 캐시 저장 완료: key={}, jsonLength={}", cacheKey, recommendationJson.length());
      } catch (Exception e) {
        log.warn("개별 배치 실패 : userId = {}, weatherId = {}", user.getId(), weather.getId(), e);
      }
    }
  }

  private String generateBatchCacheKey(UUID weatherId, UUID userId) {
    return "aiRecommendation::" + weatherId + ":" + userId;
  }
}
