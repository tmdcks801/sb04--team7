package com.example.ootd.batch.service;

import com.example.ootd.batch.listener.WeatherAlertType;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

// 날씨 알림 중복 방지 Spring Cache를 활용하여 동일한 조건의 알림 중복 발송을 방지
@Slf4j
@Service
public class WeatherAlertService {

  private static final String WEATHER_ALERT_CACHE = "weatherAlerts";

  @Autowired
  private CacheManager cacheManager;

  /**
   * 알림 발송 기록 저장 및 중복 확인
   *
   * @param regionName   지역명 (예: "서울특별시 마포구")
   * @param alertType    알림 타입 (예: HEAT_WAVE, HEAVY_RAIN)
   * @param forecastDate 예보 날짜
   * @return 항상 true 반환 (캐시 존재 여부로 중복 판단)
   */
  @Cacheable(value = WEATHER_ALERT_CACHE,
      key = "#regionName + '_' + #alertType.name() + '_' + #forecastDate",
      condition = "#regionName != null && #alertType != null && #forecastDate != null")
  public boolean markAlertAsSent(String regionName, WeatherAlertType alertType,
      LocalDate forecastDate) {
    log.debug("Marking alert as sent: {} - {} - {}", regionName, alertType, forecastDate);
    return true;
  }

  /**
   * 발송 가능한 알림이 있는지 확인 (직접 캐시 조회 방식)
   *
   * @param regionName   지역명
   * @param alertTypes   확인할 알림 타입 목록
   * @param forecastDate 예보 날짜
   * @return 하나라도 발송되지 않은 알림이 있으면 true
   */
  public boolean hasUnsentAlerts(String regionName, List<WeatherAlertType> alertTypes,
      LocalDate forecastDate) {
    Cache cache = cacheManager.getCache(WEATHER_ALERT_CACHE);
    if (cache == null) {
      log.warn("Weather alert cache not found, allowing all alerts");
      return true;
    }

    return alertTypes.stream().anyMatch(alertType -> {
      String cacheKey = buildCacheKey(regionName, alertType, forecastDate);
      boolean alreadySent = cache.get(cacheKey) != null;

      if (!alreadySent) {
        // 캐시에 없으면 발송 예정 표시
        cache.put(cacheKey, System.currentTimeMillis());
        log.debug("Alert marked for sending: {}", cacheKey);
        return true; // 발송 필요
      } else {
        log.debug("Alert already sent, skipping: {}", cacheKey);
        return false; // 이미 발송됨
      }
    });
  }

  /**
   * 특정 알림이 이미 발송되었는지 확인 (조회만)
   *
   * @param regionName   지역명
   * @param alertType    알림 타입
   * @param forecastDate 예보 날짜
   * @return 이미 발송되었으면 true
   */
  public boolean isAlertAlreadySent(String regionName, WeatherAlertType alertType,
      LocalDate forecastDate) {
    try {
      Cache cache = cacheManager.getCache(WEATHER_ALERT_CACHE);
      if (cache == null) {
        return false;
      }

      String cacheKey = buildCacheKey(regionName, alertType, forecastDate);
      return cache.get(cacheKey) != null;
    } catch (Exception e) {
      log.error("Error checking if alert was already sent", e);
      return false;
    }
  }

  /**
   * 캐시 키 생성
   */
  private String buildCacheKey(String regionName, WeatherAlertType alertType,
      LocalDate forecastDate) {
    return String.format("%s_%s_%s", regionName, alertType.name(), forecastDate);
  }

  /**
   * 캐시 통계 조회
   */
  public void logCacheStatistics() {
    try {
      Cache cache = cacheManager.getCache(WEATHER_ALERT_CACHE);
      if (cache != null
          && cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
        @SuppressWarnings("unchecked")
        com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache =
            (com.github.benmanes.caffeine.cache.Cache<Object, Object>) cache.getNativeCache();

        var stats = caffeineCache.stats();
        log.info("Weather Alert Cache Statistics - " +
                "Size: {}, Hit Rate: {:.2f}%, Miss Rate: {:.2f}%, " +
                "Evictions: {}, Average Load Time: {:.2f}ms",
            caffeineCache.estimatedSize(),
            stats.hitRate() * 100,
            stats.missRate() * 100,
            stats.evictionCount(),
            stats.averageLoadPenalty() / 1_000_000.0);
      } else {
        log.info("Cache statistics not available (using Redis or cache not found)");
      }
    } catch (Exception e) {
      log.error("Error retrieving cache statistics", e);
    }
  }

  /**
   * 캐시 정리 (필요시 수동 호출)
   */
  public void clearExpiredAlerts() {
    try {
      Cache cache = cacheManager.getCache(WEATHER_ALERT_CACHE);
      if (cache != null) {
        cache.clear();
        log.info("Weather alert cache cleared manually");
      }
    } catch (Exception e) {
      log.error("Error clearing weather alert cache", e);
    }
  }
}