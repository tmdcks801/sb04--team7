package com.example.ootd.domain.location.api;

import com.example.ootd.exception.location.LocationApiException;
import com.example.ootd.exception.location.LocationCoordinateOutOfRangeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KakaoApiClient 테스트")
class KakaoApiClientTest {

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private KakaoApiClient kakaoApiClient;

    @BeforeEach
    void setUp() {
        kakaoApiClient = new KakaoApiClient(restClientBuilder);
        ReflectionTestUtils.setField(kakaoApiClient, "kakaoApiKey", "test-kakao-key");
    }

    @Test
    @DisplayName("정상적인 행정구역 조회 성공")
    void getAdministrativeRegions_Success() {
        // Given
        double longitude = 129.0756;
        double latitude = 35.1595;
        
        KakaoApiResponse mockResponse = createMockKakaoResponse();
        
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(KakaoApiResponse.class)).thenReturn(mockResponse);

        // When
        List<KakaoApiResponse.Document> result = kakaoApiClient.getAdministrativeRegions(longitude, latitude);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).region_1depth_name()).isEqualTo("부산광역시");
        assertThat(result.get(0).region_2depth_name()).isEqualTo("남구");
        assertThat(result.get(0).region_3depth_name()).isEqualTo("대연동");
        assertThat(result.get(0).region_type()).isEqualTo("H");
        
        verify(restClientBuilder).baseUrl(anyString());
        verify(restClientBuilder).defaultHeader(eq("Authorization"), anyString());
        verify(restClient).get();
    }

    @Test
    @DisplayName("좌표 범위 초과시 LocationCoordinateOutOfRangeException 발생")
    void getAdministrativeRegions_CoordinateOutOfRange() {
        // Given
        double invalidLongitude = 200.0; // 경도 범위 초과
        double latitude = 35.1595;

        // When & Then
        assertThatThrownBy(() -> 
            kakaoApiClient.getAdministrativeRegions(invalidLongitude, latitude))
            .isInstanceOf(LocationCoordinateOutOfRangeException.class);

        verifyNoInteractions(restClientBuilder);
    }

    @Test
    @DisplayName("한국 외 지역 좌표로 LocationCoordinateOutOfRangeException 발생")
    void getAdministrativeRegions_OutsideKorea() {
        // Given
        double longitude = 120.0; // 한국 범위 밖
        double latitude = 25.0;   // 한국 범위 밖

        // When & Then
        assertThatThrownBy(() -> 
            kakaoApiClient.getAdministrativeRegions(longitude, latitude))
            .isInstanceOf(LocationCoordinateOutOfRangeException.class);

        verifyNoInteractions(restClientBuilder);
    }

    @Test
    @DisplayName("Kakao API 응답이 null인 경우 LocationApiException 발생")
    void getAdministrativeRegions_NullResponse() {
        // Given
        double longitude = 129.0756;
        double latitude = 35.1595;
        
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(KakaoApiResponse.class)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> 
            kakaoApiClient.getAdministrativeRegions(longitude, latitude))
            .isInstanceOf(LocationApiException.class);
    }

    @Test
    @DisplayName("Kakao API 응답이 빈 문서 리스트인 경우 LocationApiException 발생")
    void getAdministrativeRegions_EmptyDocuments() {
        // Given
        double longitude = 129.0756;
        double latitude = 35.1595;
        
        KakaoApiResponse emptyResponse = new KakaoApiResponse(List.of());
        
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(KakaoApiResponse.class)).thenReturn(emptyResponse);

        // When & Then
        assertThatThrownBy(() -> 
            kakaoApiClient.getAdministrativeRegions(longitude, latitude))
            .isInstanceOf(LocationApiException.class);
    }

    @Test
    @DisplayName("RestClient 호출 실패시 LocationApiException 발생")
    void getAdministrativeRegions_RestClientFailure() {
        // Given
        double longitude = 129.0756;
        double latitude = 35.1595;
        
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(KakaoApiResponse.class))
            .thenThrow(new RuntimeException("Network error"));

        // When & Then
        assertThatThrownBy(() -> 
            kakaoApiClient.getAdministrativeRegions(longitude, latitude))
            .isInstanceOf(LocationApiException.class);
    }

    @Test
    @DisplayName("H 타입 문서만 필터링하여 반환")
    void getAdministrativeRegions_FilterHType() {
        // Given
        double longitude = 129.0756;
        double latitude = 35.1595;
        
        KakaoApiResponse.Document hTypeDoc = new KakaoApiResponse.Document(
            "H", "1144011000", "부산광역시 남구 대연동", "부산광역시", "남구", "대연동", "", 129.0756, 35.1595
        );
        KakaoApiResponse.Document bTypeDoc = new KakaoApiResponse.Document(
            "B", "1144000000", "부산광역시 남구", "부산광역시", "남구", "", "", 129.0756, 35.1595
        );
        
        KakaoApiResponse mockResponse = new KakaoApiResponse(List.of(hTypeDoc, bTypeDoc));
        
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(KakaoApiResponse.class)).thenReturn(mockResponse);

        // When
        List<KakaoApiResponse.Document> result = kakaoApiClient.getAdministrativeRegions(longitude, latitude);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).region_type()).isEqualTo("H");
        assertThat(result.get(0).region_1depth_name()).isEqualTo("부산광역시");
    }

    private KakaoApiResponse createMockKakaoResponse() {
        KakaoApiResponse.Document document = new KakaoApiResponse.Document(
            "H", "1144011000", "부산광역시 남구 대연동", "부산광역시", "남구", "대연동", "", 129.0756, 35.1595
        );
        return new KakaoApiResponse(List.of(document));
    }
}
