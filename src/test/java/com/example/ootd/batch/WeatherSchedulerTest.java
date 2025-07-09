package com.example.ootd.batch;

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
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeatherScheduler 테스트")
class WeatherSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job weatherCollectJob;

    @InjectMocks
    private WeatherScheduler weatherScheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(weatherScheduler, "batchEnabled", true);
        ReflectionTestUtils.setField(weatherScheduler, "numOfRows", 1000);
    }

    @Test
    @DisplayName("배치 작업 정상 실행")
    void runWeatherBatchJob_Success() throws Exception {
        // when
        weatherScheduler.runWeatherBatchJob();

        // then
        verify(jobLauncher, times(1)).run(eq(weatherCollectJob), any(JobParameters.class));
    }

    @Test
    @DisplayName("배치 비활성화 시 실행하지 않음")
    void runWeatherBatchJob_Disabled() throws Exception {
        // given
        ReflectionTestUtils.setField(weatherScheduler, "batchEnabled", false);

        // when
        weatherScheduler.runWeatherBatchJob();

        // then
        verify(jobLauncher, never()).run(any(Job.class), any(JobParameters.class));
    }

    @Test
    @DisplayName("JobLauncher 예외 발생 시 처리")
    void runWeatherBatchJob_JobLauncherException() throws Exception {
        // given
        when(jobLauncher.run(eq(weatherCollectJob), any(JobParameters.class)))
            .thenThrow(new RuntimeException("Job execution failed"));

        // when & then (예외가 발생해도 메서드가 정상 종료되어야 함)
        weatherScheduler.runWeatherBatchJob();

        verify(jobLauncher, times(1)).run(eq(weatherCollectJob), any(JobParameters.class));
    }

    @Test
    @DisplayName("수동 실행 테스트")
    void runWeatherBatchJobManually() throws Exception {
        // when
        weatherScheduler.runWeatherBatchJobManually();

        // then
        verify(jobLauncher, times(1)).run(eq(weatherCollectJob), any(JobParameters.class));
    }
}
