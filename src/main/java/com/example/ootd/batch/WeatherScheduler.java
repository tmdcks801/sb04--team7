package com.example.ootd.batch;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class WeatherScheduler {

  private final JobLauncher jobLauncher;
  private final Job weatherCollectJob;

  @Value("${weather.batch.enabled:true}")
  private boolean batchEnabled;

  @Value("${weather.batch.numOfRows:1000}")
  private int numOfRows;

  // 매일 새벽 2시, 5시, 8시, 11시, 14시, 17시, 20시, 23시에 실행 (3시간 간격)
  @Scheduled(cron = "${weather.batch.schedule.cron:0 30 2,5,8,11,14,17,20,23 * * *}")
  public void runWeatherBatchJob() {
    if (!batchEnabled) {
      log.info("Weather batch job is disabled");
      return;
    }

    try {
      log.info("Starting weather batch job at {}", LocalDateTime.now());

      JobParameters jobParameters = new JobParametersBuilder()
          .addLocalDateTime("executedAt", LocalDateTime.now())
          .addLong("numOfRows", (long) numOfRows)
          .toJobParameters();

      jobLauncher.run(weatherCollectJob, jobParameters);

    } catch (Exception e) {
      log.error("Failed to start weather batch job", e);
    }
  }

  // 수동 실행을 위한 메서드
  public void runWeatherBatchJobManually() {
    runWeatherBatchJob();
  }
}