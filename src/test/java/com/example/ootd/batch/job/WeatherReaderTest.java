package com.example.ootd.batch.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.ootd.batch.dto.RegionInfo;
import com.example.ootd.batch.dto.WeatherBatchData;
import com.example.ootd.domain.weather.api.WeatherApiClient;
import com.example.ootd.domain.weather.api.WeatherApiResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WeatherReaderTest {

    @Mock
    private WeatherApiClient weatherApiClient;

    @InjectMocks
    private WeatherReader weatherReader;

    private List<RegionInfo> testRegions;
    private List<WeatherApiResponse.Item> mockCurrentItems;
    private List<WeatherApiResponse.Item> mockPreviousItems;

    @BeforeEach
    void setUp() {
        testRegions = List.of(
                RegionInfo.builder()
                        .regionName("서울특별시 마포구")
                        .nx(59)
                        .ny(127)
                        .build(),
                RegionInfo.builder()
                        .regionName("부산광역시 해운대구")
                        .nx(98)
                        .ny(76)
                        .build()
        );

        mockCurrentItems = List.of(
                new WeatherApiResponse.Item("20241211", "1400", "TMP", "POP", "25", "60", 59, 127),
                new WeatherApiResponse.Item("20241211", "1500", "TMP", "POP", "26", "55", 59, 127)
        );

        mockPreviousItems = List.of(
                new WeatherApiResponse.Item("20241210", "0200", "TMN", "TMX", "15", "28", 59, 127)
        );

        // 필드 값 설정
        ReflectionTestUtils.setField(weatherReader, "regions", testRegions);
        ReflectionTestUtils.setField(weatherReader, "partitionId", 1);
        ReflectionTestUtils.setField(weatherReader, "numOfRows", 1000);
        ReflectionTestUtils.setField(weatherReader, "apiDelayMs", 0L); // 테스트 시 지연 없음
    }

    @Test
    @DisplayName("정상적인 날씨 데이터 읽기 테스트")
    void read_ShouldReturnWeatherBatchData() throws Exception {
        // Given
        when(weatherApiClient.getVillageForecast(anyInt(), anyInt(), anyInt()))
                .thenReturn(mockCurrentItems);
        when(weatherApiClient.getTemperatureMinMax(anyInt(), anyInt()))
                .thenReturn(mockPreviousItems);

        // When
        WeatherBatchData result1 = weatherReader.read();
        WeatherBatchData result2 = weatherReader.read();

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(result1.getRegionName().contains("마포구") || result1.getRegionName().contains("해운대구"));
        assertTrue(result2.getRegionName().contains("마포구") || result2.getRegionName().contains("해운대구"));
    }

    @Test
    @DisplayName("모든 데이터를 읽은 후 null 반환")
    void read_AfterAllData_ShouldReturnNull() throws Exception {
        // Given
        when(weatherApiClient.getVillageForecast(anyInt(), anyInt(), anyInt()))
                .thenReturn(mockCurrentItems);
        when(weatherApiClient.getTemperatureMinMax(anyInt(), anyInt()))
                .thenReturn(mockPreviousItems);

        // When - 모든 데이터 읽기
        WeatherBatchData result1 = weatherReader.read();
        WeatherBatchData result2 = weatherReader.read();
        WeatherBatchData result3 = weatherReader.read();
        WeatherBatchData result4 = weatherReader.read();
        WeatherBatchData result5 = weatherReader.read(); // 추가로 읽어보기

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertNotNull(result4);
        assertNull(result5); // 더 이상 읽을 데이터가 없으면 null
    }

    @Test
    @DisplayName("API 호출 실패 시 해당 지역 데이터 제외")
    void read_WithApiFailure_ShouldSkipFailedRegion() throws Exception {
        // Given
        when(weatherApiClient.getVillageForecast(59, 127, 1000))
                .thenThrow(new RuntimeException("API Error"));
        when(weatherApiClient.getVillageForecast(98, 76, 1000))
                .thenReturn(mockCurrentItems);
        when(weatherApiClient.getTemperatureMinMax(anyInt(), anyInt()))
                .thenReturn(mockPreviousItems);

        // When
        WeatherBatchData result1 = weatherReader.read();
        WeatherBatchData result2 = weatherReader.read();
        WeatherBatchData result3 = weatherReader.read();

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertNull(result3); // 실패한 지역은 제외되어 더 적은 데이터
        assertEquals("부산광역시 해운대구", result1.getRegionName());
        assertEquals("부산광역시 해운대구", result2.getRegionName());
    }

    @Test
    @DisplayName("빈 지역 목록으로 초기화 시 null 반환")
    void read_WithEmptyRegions_ShouldReturnNull() throws Exception {
        // Given
        ReflectionTestUtils.setField(weatherReader, "regions", List.of());

        // When
        WeatherBatchData result = weatherReader.read();

        // Then
        assertNull(result);
        verify(weatherApiClient, never()).getVillageForecast(anyInt(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("API 지연 설정 테스트")
    void read_WithApiDelay_ShouldApplyDelay() throws Exception {
        // Given
        ReflectionTestUtils.setField(weatherReader, "apiDelayMs", 50L);
        when(weatherApiClient.getVillageForecast(anyInt(), anyInt(), anyInt()))
                .thenReturn(mockCurrentItems);
        when(weatherApiClient.getTemperatureMinMax(anyInt(), anyInt()))
                .thenReturn(mockPreviousItems);

        long startTime = System.currentTimeMillis();

        // When
        weatherReader.read();
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Then - 지연이 적용되어 실행 시간이 늘어남 (최소 2개 지역 * 50ms = 100ms)
        assertTrue(executionTime >= 90); // 약간의 여유를 둠
    }

    @Test
    @DisplayName("스레드 인터럽트 발생 시 정상 처리")
    void read_WithThreadInterrupt_ShouldHandleInterruption() throws Exception {
        // Given
        ReflectionTestUtils.setField(weatherReader, "apiDelayMs", 1000L); // 긴 지연
        when(weatherApiClient.getVillageForecast(anyInt(), anyInt(), anyInt()))
                .thenReturn(mockCurrentItems);
        when(weatherApiClient.getTemperatureMinMax(anyInt(), anyInt()))
                .thenReturn(mockPreviousItems);

        // 현재 스레드를 인터럽트
        Thread.currentThread().interrupt();

        // When & Then
        assertDoesNotThrow(() -> weatherReader.read());
        
        // 인터럽트 상태 정리
        Thread.interrupted();
    }

    @Test
    @DisplayName("파티션 ID와 지역 수 로그 확인")
    void read_ShouldLogPartitionInfo() throws Exception {
        // Given
        when(weatherApiClient.getVillageForecast(anyInt(), anyInt(), anyInt()))
                .thenReturn(mockCurrentItems);
        when(weatherApiClient.getTemperatureMinMax(anyInt(), anyInt()))
                .thenReturn(mockPreviousItems);

        // When
        weatherReader.read();

        // Then
        verify(weatherApiClient, times(2)).getVillageForecast(anyInt(), anyInt(), anyInt());
        verify(weatherApiClient, times(2)).getTemperatureMinMax(anyInt(), anyInt());
    }

    @Test
    @DisplayName("WeatherBatchData 필드 설정 확인")
    void read_ShouldSetCorrectFields() throws Exception {
        // Given
        when(weatherApiClient.getVillageForecast(anyInt(), anyInt(), anyInt()))
                .thenReturn(mockCurrentItems);
        when(weatherApiClient.getTemperatureMinMax(anyInt(), anyInt()))
                .thenReturn(mockPreviousItems);

        // When
        WeatherBatchData result = weatherReader.read();

        // Then
        assertNotNull(result);
        assertNotNull(result.getRegionName());
        assertNotNull(result.getNx());
        assertNotNull(result.getNy());
        assertNotNull(result.getItems());
        assertNotNull(result.getPreviousItems());
        assertTrue(result.getNx() > 0);
        assertTrue(result.getNy() > 0);
        assertFalse(result.getItems().isEmpty());
        assertFalse(result.getPreviousItems().isEmpty());
    }
}
