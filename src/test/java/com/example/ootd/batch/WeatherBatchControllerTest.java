package com.example.ootd.batch;

import com.example.ootd.batch.WeatherBatchController;
import com.example.ootd.batch.WeatherCleanupScheduler;
import com.example.ootd.batch.WeatherScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeatherBatchController 테스트")
class WeatherBatchControllerTest {

    @Mock
    private WeatherCleanupScheduler weatherCleanupScheduler;

    @Mock
    private WeatherScheduler weatherBatchScheduler;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job weatherCollectJob;

    @InjectMocks
    private WeatherBatchController weatherBatchController;

    @Test
    @DisplayName("날씨 배치 수동 실행 성공")
    void runWeatherBatch_Success() {
        // Given
        doNothing().when(weatherBatchScheduler).runWeatherBatchJobManually();

        // When
        ResponseEntity<String> response = weatherBatchController.runWeatherBatch();

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Weather batch job started successfully");
        
        verify(weatherBatchScheduler, times(1)).runWeatherBatchJobManually();
    }

    @Test
    @DisplayName("날씨 배치 수동 실행 실패")
    void runWeatherBatch_Failure() {
        // Given
        RuntimeException exception = new RuntimeException("배치 작업 실행 실패");
        doThrow(exception).when(weatherBatchScheduler).runWeatherBatchJobManually();

        // When
        ResponseEntity<String> response = weatherBatchController.runWeatherBatch();

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(500);
        assertThat(response.getBody()).contains("Failed to start weather batch job");
        assertThat(response.getBody()).contains("배치 작업 실행 실패");
        
        verify(weatherBatchScheduler, times(1)).runWeatherBatchJobManually();
    }

    @Test
    @DisplayName("실패한 지역 재시도 성공")
    void retryFailedRegions_Success() throws Exception {
        // Given
        List<String> failedRegions = Arrays.asList("서울특별시 강남구", "부산광역시 해운대구");
        
        // When
        ResponseEntity<String> response = weatherBatchController.retryFailedRegions(failedRegions);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).contains("Retry job started for regions");
        assertThat(response.getBody()).contains("서울특별시 강남구");
        assertThat(response.getBody()).contains("부산광역시 해운대구");
        
        verify(jobLauncher, times(1)).run(eq(weatherCollectJob), any(JobParameters.class));
    }

    @Test
    @DisplayName("실패한 지역 재시도 실패")
    void retryFailedRegions_Failure() throws Exception {
        // Given
        List<String> failedRegions = Arrays.asList("서울특별시 강남구");
        RuntimeException exception = new RuntimeException("Job launcher 실행 실패");
        
        when(jobLauncher.run(eq(weatherCollectJob), any(JobParameters.class)))
            .thenThrow(exception);

        // When
        ResponseEntity<String> response = weatherBatchController.retryFailedRegions(failedRegions);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(500);
        assertThat(response.getBody()).contains("Failed to start retry job");
        assertThat(response.getBody()).contains("Job launcher 실행 실패");
        
        verify(jobLauncher, times(1)).run(eq(weatherCollectJob), any(JobParameters.class));
    }

    @Test
    @DisplayName("빈 실패 지역 리스트로 재시도")
    void retryFailedRegions_EmptyList() throws Exception {
        // Given
        List<String> emptyFailedRegions = Arrays.asList();

        // When
        ResponseEntity<String> response = weatherBatchController.retryFailedRegions(emptyFailedRegions);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).contains("Retry job started for regions: []");
        
        verify(jobLauncher, times(1)).run(eq(weatherCollectJob), any(JobParameters.class));
    }

    @Test
    @DisplayName("수동 정리 성공 - 기본 보관 일수")
    void manualCleanup_Success_DefaultDays() {
        // Given
        doNothing().when(weatherCleanupScheduler).manualCleanup(2);

        // When
        ResponseEntity<Map<String, String>> response = weatherBatchController.manualCleanup(2);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).containsKey("message");
        assertThat(response.getBody()).containsKey("daysKept");
        assertThat(response.getBody()).containsKey("timestamp");
        assertThat(response.getBody().get("message")).isEqualTo("Manual cleanup completed successfully");
        assertThat(response.getBody().get("daysKept")).isEqualTo("2");
        
        verify(weatherCleanupScheduler, times(1)).manualCleanup(2);
    }

    @Test
    @DisplayName("수동 정리 성공 - 사용자 지정 보관 일수")
    void manualCleanup_Success_CustomDays() {
        // Given
        int customDays = 5;
        doNothing().when(weatherCleanupScheduler).manualCleanup(customDays);

        // When
        ResponseEntity<Map<String, String>> response = weatherBatchController.manualCleanup(customDays);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().get("daysKept")).isEqualTo("5");
        
        verify(weatherCleanupScheduler, times(1)).manualCleanup(customDays);
    }

    @Test
    @DisplayName("수동 정리 실패")
    void manualCleanup_Failure() {
        // Given
        int daysToKeep = 3;
        RuntimeException exception = new RuntimeException("정리 작업 실행 실패");
        doThrow(exception).when(weatherCleanupScheduler).manualCleanup(daysToKeep);

        // When
        ResponseEntity<Map<String, String>> response = weatherBatchController.manualCleanup(daysToKeep);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(500);
        assertThat(response.getBody()).containsKey("error");
        assertThat(response.getBody()).containsKey("timestamp");
        assertThat(response.getBody().get("error")).contains("Manual cleanup failed");
        assertThat(response.getBody().get("error")).contains("정리 작업 실행 실패");
        
        verify(weatherCleanupScheduler, times(1)).manualCleanup(daysToKeep);
    }

    @Test
    @DisplayName("JobParameters 생성 검증 - retry 요청")
    void retryFailedRegions_JobParametersValidation() throws Exception {
        // Given
        List<String> failedRegions = Arrays.asList("제주특별자치도 제주시");

        // When
        weatherBatchController.retryFailedRegions(failedRegions);

        // Then
        verify(jobLauncher).run(eq(weatherCollectJob), argThat(jobParameters -> {
            // JobParameters 내용 검증
            String retryRegions = jobParameters.getString("retryRegions");
            String jobType = jobParameters.getString("jobType");
            Long numOfRows = jobParameters.getLong("numOfRows");
            
            return "제주특별자치도 제주시".equals(retryRegions) &&
                   "RETRY".equals(jobType) &&
                   1000L == numOfRows;
        }));
    }
}
