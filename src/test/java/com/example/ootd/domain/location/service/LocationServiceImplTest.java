package com.example.ootd.domain.location.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.ootd.domain.location.Location;
import com.example.ootd.domain.location.api.KakaoApiClient;
import com.example.ootd.domain.location.api.KakaoApiResponse;
import com.example.ootd.domain.location.dto.WeatherAPILocation;
import com.example.ootd.domain.location.repository.LocationRepository;
import com.example.ootd.exception.location.LocationNotFoundException;
import com.example.ootd.exception.location.LocationRegionInfoInsufficientException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocationService 통합 테스트")
class LocationServiceImplTest {

  @Mock
  private KakaoApiClient kakaoApiClient;

  @Mock
  private LocationRepository locationRepository;

  @InjectMocks
  private LocationServiceImpl locationService;

  private double testLatitude;
  private double testLongitude;
  private Location mockLocation;
  private List<KakaoApiResponse.Document> mockDocuments;

  @BeforeEach
  void setUp() {
    testLatitude = 35.1595;
    testLongitude = 129.0756;

    List<String> locationNames = List.of("부산광역시", "남구", "대연동", "");
    mockLocation = new Location(testLatitude, testLongitude, 98, 75, locationNames);

    KakaoApiResponse.Document document = new KakaoApiResponse.Document(
        "H", "1144011000", "부산광역시 남구 대연동", "부산광역시", "남구", "대연동", "", 129.0756, 35.1595
    );
    mockDocuments = List.of(document);
  }

  @Test
  @DisplayName("DB에 위치 정보가 있는 경우 캐시된 데이터 반환")
  void getGridAndLocation_FromDatabase() {
    // Given
    when(locationRepository.findByLatitudeAndLongitude(testLatitude, testLongitude))
        .thenReturn(mockLocation);

    // When
    WeatherAPILocation result = locationService.getGridAndLocation(testLatitude, testLongitude);

    // Then
    assertThat(result.latitude()).isEqualTo(testLatitude);
    assertThat(result.longitude()).isEqualTo(testLongitude);
    assertThat(result.x()).isEqualTo(98);
    assertThat(result.y()).isEqualTo(75);
    assertThat(result.locationNames()).hasSize(4);
    assertThat(result.locationNames().get(0)).isEqualTo("부산광역시");
    assertThat(result.locationNames().get(1)).isEqualTo("남구");

    verify(locationRepository).findByLatitudeAndLongitude(testLatitude, testLongitude);
    verifyNoInteractions(kakaoApiClient);
  }

  @Test
  @DisplayName("DB에 위치 정보가 없는 경우 Kakao API 호출 후 저장")
  void getGridAndLocation_FromKakaoApi() {
    // Given
    when(locationRepository.findByLatitudeAndLongitude(testLatitude, testLongitude))
        .thenReturn(null);
    when(kakaoApiClient.getAdministrativeRegions(testLongitude, testLatitude))
        .thenReturn(mockDocuments);
    when(locationRepository.save(any(Location.class)))
        .thenReturn(mockLocation);

    // When
    WeatherAPILocation result = locationService.getGridAndLocation(testLatitude, testLongitude);

    // Then
    assertThat(result.latitude()).isEqualTo(testLatitude);
    assertThat(result.longitude()).isEqualTo(testLongitude);
    assertThat(result.x()).isEqualTo(98);
    assertThat(result.y()).isEqualTo(75);
    assertThat(result.locationNames()).hasSize(4);

    verify(locationRepository).findByLatitudeAndLongitude(testLatitude, testLongitude);
    verify(kakaoApiClient).getAdministrativeRegions(testLongitude, testLatitude);
    verify(locationRepository).save(any(Location.class));
  }

  @Test
  @DisplayName("Kakao API에서 빈 응답인 경우 LocationRegionInfoInsufficientException 발생")
  void getGridAndLocation_EmptyKakaoResponse() {
    // Given
    when(locationRepository.findByLatitudeAndLongitude(testLatitude, testLongitude))
        .thenReturn(null);
    when(kakaoApiClient.getAdministrativeRegions(testLongitude, testLatitude))
        .thenReturn(List.of());

    // When & Then
    assertThatThrownBy(() ->
        locationService.getGridAndLocation(testLatitude, testLongitude))
        .isInstanceOf(LocationRegionInfoInsufficientException.class);
  }

  @Test
  @DisplayName("Kakao API 호출 실패시 LocationNotFoundException 발생")
  void getGridAndLocation_KakaoApiFailure() {
    // Given
    when(locationRepository.findByLatitudeAndLongitude(testLatitude, testLongitude))
        .thenReturn(null);
    when(kakaoApiClient.getAdministrativeRegions(testLongitude, testLatitude))
        .thenThrow(new RuntimeException("Kakao API error"));

    // When & Then
    assertThatThrownBy(() ->
        locationService.getGridAndLocation(testLatitude, testLongitude))
        .isInstanceOf(LocationNotFoundException.class);

    verify(locationRepository).findByLatitudeAndLongitude(testLatitude, testLongitude);
    verify(kakaoApiClient).getAdministrativeRegions(testLongitude, testLatitude);
    verify(locationRepository, never()).save(any());
  }

  @Test
  @DisplayName("좌표 변환 정확성 검증")
  void getGridAndLocation_GridConversionAccuracy() {
    // Given
    when(locationRepository.findByLatitudeAndLongitude(testLatitude, testLongitude))
        .thenReturn(null);
    when(kakaoApiClient.getAdministrativeRegions(testLongitude, testLatitude))
        .thenReturn(mockDocuments);
    when(locationRepository.save(any(Location.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    WeatherAPILocation result = locationService.getGridAndLocation(testLatitude, testLongitude);

    // Then
    // 부산 남구 좌표의 격자 변환 결과 검증
    assertThat(result.x()).isEqualTo(98);
    assertThat(result.y()).isEqualTo(75);
  }
}
