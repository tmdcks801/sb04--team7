package com.example.ootd.batch;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/batch/weather")
@RequiredArgsConstructor
public class WeatherBatchController {

  private final WeatherCleanupScheduler weatherCleanupScheduler;

  private final WeatherScheduler weatherBatchScheduler;
  private final JobLauncher jobLauncher;
  private final Job weatherCollectJob;

  @PostMapping("/run")
  public ResponseEntity<String> runWeatherBatch() {
    try {
      weatherBatchScheduler.runWeatherBatchJobManually();
      return ResponseEntity.ok("Weather batch job started successfully");
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body("Failed to start weather batch job: " + e.getMessage());
    }
  }

  @PostMapping("/retry")
  public ResponseEntity<String> retryFailedRegions(@RequestBody List<String> failedRegionNames) {
    try {
      // 실패한 지역만으로 새로운 Job 실행
      JobParameters jobParameters = new JobParametersBuilder()
          .addLocalDateTime("executedAt", LocalDateTime.now())
          .addString("retryRegions", String.join(",", failedRegionNames))
          .addString("jobType", "RETRY")
          .addLong("numOfRows", 1000L)
          .toJobParameters();

      jobLauncher.run(weatherCollectJob, jobParameters);

      log.info("Retry job started for regions: {}", failedRegionNames);
      return ResponseEntity.ok("Retry job started for regions: " + failedRegionNames);
    } catch (Exception e) {
      log.error("Failed to start retry job", e);
      return ResponseEntity.internalServerError()
          .body("Failed to start retry job: " + e.getMessage());
    }
  }


  // 수동으로 날씨 삭제
  @PostMapping("/manual")
  public ResponseEntity<Map<String, String>> manualCleanup(
      @RequestParam(defaultValue = "2") int daysToKeep) {

    try {
      weatherCleanupScheduler.manualCleanup(daysToKeep);

      return ResponseEntity.ok(Map.of(
          "message", "Manual cleanup completed successfully",
          "daysKept", String.valueOf(daysToKeep),
          "timestamp", java.time.LocalDateTime.now().toString()
      ));

    } catch (Exception e) {
      log.error("수동 삭제 실패", e);

      return ResponseEntity.internalServerError().body(Map.of(
          "error", "Manual cleanup failed: " + e.getMessage(),
          "timestamp", java.time.LocalDateTime.now().toString()
      ));
    }
  }

}