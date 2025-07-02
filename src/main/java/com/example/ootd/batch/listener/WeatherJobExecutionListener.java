package com.example.ootd.batch.listener;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

@Slf4j
public class WeatherJobExecutionListener implements JobExecutionListener {

  @Override
  public void beforeJob(JobExecution jobExecution) {
    log.info("=== Weather Batch Job Started ===");
    log.info("Job Name: {}", jobExecution.getJobInstance().getJobName());
    log.info("Job Instance Id: {}", jobExecution.getJobInstance().getInstanceId());
    log.info("Job Execution Id: {}", jobExecution.getId());
    log.info("Job Parameters: {}", jobExecution.getJobParameters());
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    LocalDateTime startTime = jobExecution.getStartTime();
    LocalDateTime endTime = jobExecution.getEndTime();
    Duration duration = Duration.between(startTime, endTime);

    log.info("=== Weather Batch Job Completed ===");
    log.info("Job Status: {}", jobExecution.getStatus());
    log.info("Exit Status: {}", jobExecution.getExitStatus());
    log.info("Start Time: {}", startTime);
    log.info("End Time: {}", endTime);
    log.info("Duration: {} seconds", duration.getSeconds());

    if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
      log.info("Weather batch job completed successfully!");
    } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
      log.error("Weather batch job failed!");
      jobExecution.getAllFailureExceptions().forEach(throwable ->
          log.error("Failure reason: ", throwable)
      );
    }
  }
}