package com.example.ootd.batch.listener;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;

class WeatherJobExecutionListenerTest {

  private WeatherJobExecutionListener listener;
  private JobExecution jobExecution;
  private JobInstance jobInstance;

  @BeforeEach
  void setUp() {
    listener = new WeatherJobExecutionListener();
    jobInstance = new JobInstance(1L, "weatherDataJob");
    jobExecution = new JobExecution(jobInstance, new JobParameters());
    jobExecution.setId(100L);
    jobExecution.setStartTime(LocalDateTime.now().minusMinutes(10));
    jobExecution.setEndTime(LocalDateTime.now());
  }

  @Test
  @DisplayName("작업 시작 전 로그 정상 출력")
  void beforeJob_ShouldLogJobStartInfo() {
    // When & Then
    assertDoesNotThrow(() -> listener.beforeJob(jobExecution));
  }

  @Test
  @DisplayName("작업 완료 후 로그 정상 출력")
  void afterJob_ShouldLogJobCompletionInfo() {
    // Given
    jobExecution.setStatus(BatchStatus.COMPLETED);

    // When & Then
    assertDoesNotThrow(() -> listener.afterJob(jobExecution));
  }

  @Test
  @DisplayName("작업 실패 시 실패 정보 로그 출력")
  void afterJob_WithFailedStatus_ShouldLogFailureInfo() {
    // Given
    jobExecution.setStatus(BatchStatus.FAILED);
    jobExecution.addFailureException(new RuntimeException("Test exception 1"));
    jobExecution.addFailureException(new IllegalArgumentException("Test exception 2"));

    // When & Then
    assertDoesNotThrow(() -> listener.afterJob(jobExecution));
  }

  @Test
  @DisplayName("작업 중단 시 상태 정보 로그 출력")
  void afterJob_WithStoppedStatus_ShouldLogStoppedInfo() {
    // Given
    jobExecution.setStatus(BatchStatus.STOPPED);

    // When & Then
    assertDoesNotThrow(() -> listener.afterJob(jobExecution));
  }

  @Test
  @DisplayName("실패 예외 목록이 비어있어도 정상 처리")
  void afterJob_WithEmptyFailureExceptions_ShouldHandleGracefully() {
    // Given
    jobExecution.setStatus(BatchStatus.FAILED);
    // 실패 예외를 추가하지 않음 (빈 리스트 상태)

    // When & Then
    assertDoesNotThrow(() -> listener.afterJob(jobExecution));
  }

  @Test
  @DisplayName("포기된 작업 상태 처리")
  void afterJob_WithAbandonedStatus_ShouldLogAbandonedInfo() {
    // Given
    jobExecution.setStatus(BatchStatus.ABANDONED);

    // When & Then
    assertDoesNotThrow(() -> listener.afterJob(jobExecution));
  }

  @Test
  @DisplayName("알 수 없는 상태 처리")
  void afterJob_WithUnknownStatus_ShouldLogUnknownInfo() {
    // Given
    jobExecution.setStatus(BatchStatus.UNKNOWN);

    // When & Then
    assertDoesNotThrow(() -> listener.afterJob(jobExecution));
  }

  @Test
  @DisplayName("여러 실패 예외가 있는 경우 모두 로그 출력")
  void afterJob_WithMultipleFailureExceptions_ShouldLogAllExceptions() {
    // Given
    jobExecution.setStatus(BatchStatus.FAILED);
    jobExecution.addFailureException(new RuntimeException("First exception"));
    jobExecution.addFailureException(new IllegalStateException("Second exception"));
    jobExecution.addFailureException(new NullPointerException("Third exception"));

    // When & Then
    assertDoesNotThrow(() -> listener.afterJob(jobExecution));

    // 실패 예외가 정상적으로 저장되었는지 확인
    List<Throwable> failureExceptions = jobExecution.getAllFailureExceptions();
    assertEquals(3, failureExceptions.size());
    assertTrue(
        failureExceptions.stream().anyMatch(e -> e.getMessage().contains("First exception")));
    assertTrue(
        failureExceptions.stream().anyMatch(e -> e.getMessage().contains("Second exception")));
    assertTrue(
        failureExceptions.stream().anyMatch(e -> e.getMessage().contains("Third exception")));
  }
}
