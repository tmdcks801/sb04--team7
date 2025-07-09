package com.example.ootd.batch;

import com.example.ootd.domain.weather.repository.WeatherRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeatherCleanupScheduler 테스트")
class WeatherCleanupSchedulerTest {

    @Mock
    private WeatherRepository weatherRepository;

    @InjectMocks
    private WeatherCleanupScheduler weatherCleanupScheduler;

    @Test
    @DisplayName("정기 날씨 데이터 정리 성공")
    void cleanupOldWeatherData_Success() {
        // given
        when(weatherRepository.deleteOldWeatherDataPreservingFeedReferences(any(LocalDateTime.class)))
            .thenReturn(100);

        // when
        weatherCleanupScheduler.cleanupOldWeatherData();

        // then
        verify(weatherRepository, times(1))
            .deleteOldWeatherDataPreservingFeedReferences(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("날씨 데이터 정리 중 예외 발생 처리")
    void cleanupOldWeatherData_Exception() {
        // given
        when(weatherRepository.deleteOldWeatherDataPreservingFeedReferences(any(LocalDateTime.class)))
            .thenThrow(new RuntimeException("Database error"));

        // when & then (예외가 발생해도 메서드가 정상 종료되어야 함)
        weatherCleanupScheduler.cleanupOldWeatherData();

        verify(weatherRepository, times(1))
            .deleteOldWeatherDataPreservingFeedReferences(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("수동 정리 성공")
    void manualCleanup_Success() {
        // given
        int daysToKeep = 7;
        when(weatherRepository.deleteOldWeatherDataPreservingFeedReferences(any(LocalDateTime.class)))
            .thenReturn(50);

        // when
        weatherCleanupScheduler.manualCleanup(daysToKeep);

        // then
        verify(weatherRepository, times(1))
            .deleteOldWeatherDataPreservingFeedReferences(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("수동 정리 중 예외 발생 처리")
    void manualCleanup_Exception() {
        // given
        int daysToKeep = 7;
        when(weatherRepository.deleteOldWeatherDataPreservingFeedReferences(any(LocalDateTime.class)))
            .thenThrow(new RuntimeException("Database error"));

        // when & then
        try {
            weatherCleanupScheduler.manualCleanup(daysToKeep);
        } catch (Exception e) {
            // 예외가 전파되는 것은 정상
        }

        verify(weatherRepository, times(1))
            .deleteOldWeatherDataPreservingFeedReferences(any(LocalDateTime.class));
    }
}
