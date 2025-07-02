package com.example.ootd.batch.listener;

import com.example.ootd.batch.WeatherBatchNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherBatchFailureListener implements JobExecutionListener {

  private final WeatherItemListener weatherItemListener;
  private final WeatherBatchNotificationService notificationService;

  @Override
  public void beforeJob(JobExecution jobExecution) {
    // 시작 전 초기화
    weatherItemListener.clearFailedRegions();
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    // 실패한 경우에만 처리
    if (jobExecution.getStatus() != BatchStatus.COMPLETED) {
      var failedRegions = weatherItemListener.getFailedRegions();
      if (!failedRegions.isEmpty()) {
        notificationService.sendFailureNotification(
            jobExecution.getJobInstance().getJobName(),
            failedRegions,
            jobExecution.getId()
        );
      }
    }
  }
}