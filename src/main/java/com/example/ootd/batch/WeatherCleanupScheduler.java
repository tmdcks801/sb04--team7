package com.example.ootd.batch;

import com.example.ootd.domain.weather.repository.WeatherRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherCleanupScheduler {

  private final WeatherRepository weatherRepository;


  @Scheduled(cron = "0 0 3 * * *")
  @Transactional
  public void cleanupOldWeatherData() {
    log.info("그제 날씨 삭제 시작");

    try {
      LocalDateTime cutoffDate = LocalDateTime.now().minusDays(2); // 그제 자정

      // 실제 삭제 실행
      int deletedCount = weatherRepository.deleteOldWeatherDataPreservingFeedReferences(cutoffDate);

      log.info("날씨 삭제 완료: {}개 삭제", deletedCount);

    } catch (Exception e) {
      log.error("Weather data cleanup failed", e);
    }
  }

  // 수동으로 날씨 삭제
  @Transactional
  public void manualCleanup(int daysToKeep) {
    log.info("수동으로 {}일 이전 날씨 삭제", daysToKeep);

    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);

    // 삭제 실행
    int deletedCount = weatherRepository.deleteOldWeatherDataPreservingFeedReferences(cutoffDate);

    log.info("수동 날씨 삭제 성공: {}개 삭제", deletedCount);
  }


}