package com.example.ootd.domain.weather.api;

import com.example.ootd.exception.weather.WeatherApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeatherApiClient 테스트")
class WeatherApiClientTest {

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private WeatherApiClient weatherApiClient;

    @BeforeEach
    void setUp() {
        weatherApiClient = new WeatherApiClient(restClientBuilder);
        ReflectionTestUtils.setField(weatherApiClient, "weatherApiKey", "test-api-key");
    }

    @Test
    @DisplayName("정상적인 날씨 예보 API 호출 성공")
    void getVillageForecast_Success() {
        // Given
        int nx = 98, ny = 76, numOfRows = 100;
        
        WeatherApiResponse mockResponse = createMockSuccessResponse();
        
        when(restClientBuilder.build()).thenReturn(restClient);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(WeatherApiResponse.class)).thenReturn(mockResponse);

        // When
        List<WeatherApiResponse.Item> result = weatherApiClient.getVillageForecast(nx, ny, numOfRows);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).category()).isEqualTo("TMP");
        assertThat(result.get(1).category()).isEqualTo("POP");
        
        verify(restClientBuilder).build();
        verify(restClient).get();
    }

    @Test
    @DisplayName("API 응답이 null인 경우 WeatherApiException 발생")
    void getVillageForecast_NullResponse() {
        // Given
        int nx = 98, ny = 76, numOfRows = 100;
        
        when(restClientBuilder.build()).thenReturn(restClient);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(WeatherApiResponse.class)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> 
            weatherApiClient.getVillageForecast(nx, ny, numOfRows))
            .isInstanceOf(WeatherApiException.class);
    }

    @Test
    @DisplayName("API 결과 코드가 실패인 경우 WeatherApiException 발생")
    void getVillageForecast_FailureResultCode() {
        // Given
        int nx = 98, ny = 76, numOfRows = 100;
        
        WeatherApiResponse mockResponse = createMockFailureResponse();
        
        when(restClientBuilder.build()).thenReturn(restClient);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(WeatherApiResponse.class)).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> 
            weatherApiClient.getVillageForecast(nx, ny, numOfRows))
            .isInstanceOf(WeatherApiException.class);
    }

    @Test
    @DisplayName("최저/최고 기온 데이터 조회 성공")
    void getTemperatureMinMax_Success() {
        // Given
        int nx = 98, ny = 76;
        
        WeatherApiResponse mockResponse = createMockTemperatureResponse();
        
        when(restClientBuilder.build()).thenReturn(restClient);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(WeatherApiResponse.class)).thenReturn(mockResponse);

        // When
        List<WeatherApiResponse.Item> result = weatherApiClient.getTemperatureMinMax(nx, ny);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(WeatherApiResponse.Item::category)
            .containsExactly("TMN", "TMX");
        
        verify(restClientBuilder).build();
        verify(restClient).get();
    }

    @Test
    @DisplayName("네트워크 오류 발생시 WeatherApiException 발생")
    void getVillageForecast_NetworkError() {
        // Given
        int nx = 98, ny = 76, numOfRows = 100;
        
        when(restClientBuilder.build()).thenReturn(restClient);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(WeatherApiResponse.class))
            .thenThrow(new RuntimeException("Network timeout"));

        // When & Then
        assertThatThrownBy(() -> 
            weatherApiClient.getVillageForecast(nx, ny, numOfRows))
            .isInstanceOf(WeatherApiException.class);
    }

    private WeatherApiResponse createMockSuccessResponse() {
        WeatherApiResponse.Item item1 = new WeatherApiResponse.Item(
            "20241215", "1400", "TMP", "20241215", "1400", "15", 98, 76
        );
        WeatherApiResponse.Item item2 = new WeatherApiResponse.Item(
            "20241215", "1400", "POP", "20241215", "1400", "20", 98, 76
        );
        
        WeatherApiResponse.Items items = new WeatherApiResponse.Items(List.of(item1, item2));
        WeatherApiResponse.Body body = new WeatherApiResponse.Body("JSON", items);
        WeatherApiResponse.Header header = new WeatherApiResponse.Header("00", "NORMAL_SERVICE");
        WeatherApiResponse.Response response = new WeatherApiResponse.Response(header, body);
        
        return new WeatherApiResponse(response);
    }

    private WeatherApiResponse createMockFailureResponse() {
        WeatherApiResponse.Items items = new WeatherApiResponse.Items(List.of());
        WeatherApiResponse.Body body = new WeatherApiResponse.Body("JSON", items);
        WeatherApiResponse.Header header = new WeatherApiResponse.Header("99", "APPLICATION_ERROR");
        WeatherApiResponse.Response response = new WeatherApiResponse.Response(header, body);
        
        return new WeatherApiResponse(response);
    }

    private WeatherApiResponse createMockTemperatureResponse() {
        WeatherApiResponse.Item tmnItem = new WeatherApiResponse.Item(
            "20241214", "0200", "TMN", "20241215", "0000", "5", 98, 76
        );
        WeatherApiResponse.Item tmxItem = new WeatherApiResponse.Item(
            "20241214", "0200", "TMX", "20241215", "0000", "20", 98, 76
        );
        
        WeatherApiResponse.Items items = new WeatherApiResponse.Items(List.of(tmnItem, tmxItem));
        WeatherApiResponse.Body body = new WeatherApiResponse.Body("JSON", items);
        WeatherApiResponse.Header header = new WeatherApiResponse.Header("00", "NORMAL_SERVICE");
        WeatherApiResponse.Response response = new WeatherApiResponse.Response(header, body);
        
        return new WeatherApiResponse(response);
    }
}
