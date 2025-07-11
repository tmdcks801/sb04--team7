package com.example.ootd.batch.listener;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;

class WeatherStepExecutionListenerTest {

    private WeatherStepExecutionListener listener;
    private StepExecution stepExecution;

    @BeforeEach
    void setUp() {
        listener = new WeatherStepExecutionListener();
        
        JobInstance jobInstance = new JobInstance(1L, "weatherDataJob");
        JobExecution jobExecution = new JobExecution(jobInstance, new JobParameters());
        stepExecution = new StepExecution("weatherProcessingStep", jobExecution);
    }

    @Test
    @DisplayName("스텝 시작 전 로그 출력")
    void beforeStep_ShouldLogStepStart() {
        // When & Then
        assertDoesNotThrow(() -> listener.beforeStep(stepExecution));
    }

    @Test
    @DisplayName("파티션 ID가 있는 경우 로그에 포함")
    void beforeStep_WithPartitionId_ShouldLogPartitionInfo() {
        // Given
        stepExecution.getExecutionContext().put("partitionId", "partition-1");

        // When & Then
        assertDoesNotThrow(() -> listener.beforeStep(stepExecution));
    }

    @Test
    @DisplayName("스텝 완료 후 통계 정보 로그 출력")
    void afterStep_ShouldLogStepStatistics() {
        // Given
        stepExecution.setReadCount(100);
        stepExecution.setWriteCount(95);
        stepExecution.setCommitCount(10);
        stepExecution.setRollbackCount(2);
        stepExecution.setFilterCount(3);
        stepExecution.setWriteSkipCount(2);
        stepExecution.setProcessSkipCount(1);
        stepExecution.setReadSkipCount(0);

        // When
        ExitStatus result = listener.afterStep(stepExecution);

        // Then
        assertNotNull(result);
        assertEquals(stepExecution.getExitStatus(), result);
    }

    @Test
    @DisplayName("스텝 실행 통계가 0인 경우도 정상 처리")
    void afterStep_WithZeroStatistics_ShouldHandleGracefully() {
        // Given
        stepExecution.setReadCount(0);
        stepExecution.setWriteCount(0);
        stepExecution.setCommitCount(0);
        stepExecution.setRollbackCount(0);

        // When
        ExitStatus result = listener.afterStep(stepExecution);

        // Then
        assertNotNull(result);
        assertEquals(stepExecution.getExitStatus(), result);
    }

    @Test
    @DisplayName("스텝 실행 실패 시에도 통계 로그 출력")
    void afterStep_WithFailedStep_ShouldLogStatistics() {
        // Given
        stepExecution.setStatus(org.springframework.batch.core.BatchStatus.FAILED);
        stepExecution.setExitStatus(ExitStatus.FAILED);
        stepExecution.setReadCount(50);
        stepExecution.setWriteCount(30);
        stepExecution.setRollbackCount(5);

        // When
        ExitStatus result = listener.afterStep(stepExecution);

        // Then
        assertNotNull(result);
        assertEquals(ExitStatus.FAILED, result);
    }

    @Test
    @DisplayName("높은 롤백 수가 있는 경우 로그 출력")
    void afterStep_WithHighRollbackCount_ShouldLogInfo() {
        // Given
        stepExecution.setReadCount(1000);
        stepExecution.setWriteCount(800);
        stepExecution.setRollbackCount(50); // 높은 롤백 수

        // When
        ExitStatus result = listener.afterStep(stepExecution);

        // Then
        assertNotNull(result);
        assertEquals(stepExecution.getExitStatus(), result);
    }

    @Test
    @DisplayName("높은 스킵 수가 있는 경우 로그 출력")
    void afterStep_WithHighSkipCount_ShouldLogInfo() {
        // Given
        stepExecution.setReadCount(1000);
        stepExecution.setWriteCount(900);
        stepExecution.setReadSkipCount(30);
        stepExecution.setProcessSkipCount(40);
        stepExecution.setWriteSkipCount(30);

        // When
        ExitStatus result = listener.afterStep(stepExecution);

        // Then
        assertNotNull(result);
        assertEquals(stepExecution.getExitStatus(), result);
    }

    @Test
    @DisplayName("커스텀 ExitStatus 반환 테스트")
    void afterStep_ShouldReturnOriginalExitStatus() {
        // Given
        ExitStatus customExitStatus = new ExitStatus("COMPLETED_WITH_WARNINGS");
        stepExecution.setExitStatus(customExitStatus);

        // When
        ExitStatus result = listener.afterStep(stepExecution);

        // Then
        assertEquals(customExitStatus, result);
    }

    @Test
    @DisplayName("스텝 이름이 긴 경우도 정상 처리")
    void beforeStep_WithLongStepName_ShouldHandleGracefully() {
        // Given
        StepExecution longNameStep = new StepExecution(
            "veryLongStepNameThatExceedsNormalLengthForTestingPurposes", 
            stepExecution.getJobExecution()
        );

        // When & Then
        assertDoesNotThrow(() -> listener.beforeStep(longNameStep));
    }

    @Test
    @DisplayName("파티션 ID가 null인 경우도 정상 처리")
    void beforeStep_WithNullPartitionId_ShouldHandleGracefully() {
        // Given
        stepExecution.getExecutionContext().put("partitionId", null);

        // When & Then
        assertDoesNotThrow(() -> listener.beforeStep(stepExecution));
    }

    @Test
    @DisplayName("ExecutionContext가 비어있는 경우도 정상 처리")
    void beforeStep_WithEmptyExecutionContext_ShouldHandleGracefully() {
        // Given - ExecutionContext는 기본적으로 비어있음

        // When & Then
        assertDoesNotThrow(() -> listener.beforeStep(stepExecution));
    }
}
