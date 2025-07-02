package com.example.ootd.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

@Slf4j
public class WeatherStepExecutionListener implements StepExecutionListener {

  @Override
  public void beforeStep(StepExecution stepExecution) {
    log.info("Starting step: {} in partition: {}",
        stepExecution.getStepName(),
        stepExecution.getExecutionContext().get("partitionId"));
  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    log.info("Step {} completed. Read: {}, Written: {}, Skipped: {}, Failed: {}",
        stepExecution.getStepName(),
        stepExecution.getReadCount(),
        stepExecution.getWriteCount(),
        stepExecution.getSkipCount(),
        stepExecution.getRollbackCount());

    return stepExecution.getExitStatus();
  }
}