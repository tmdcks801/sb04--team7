package com.example.ootd.domain.location.api;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import com.example.ootd.exception.location.LocationApiException;
import com.example.ootd.exception.location.LocationCoordinateOutOfRangeException;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoApiClient {

  private final RestClient.Builder restClientBuilder;

  @Value("${kakao.api.key}")
  private String kakaoApiKey;

  private static final String BASE_URL = "https://dapi.kakao.com";

  public List<KakaoApiResponse.Document> getAdministrativeRegions(double longitude,
      double latitude) {

    if (longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90) {
      throw new LocationCoordinateOutOfRangeException(latitude, longitude);
    }

    // 한국 내 좌표 범위 참고 검사
    if (latitude < 33.0 || latitude > 38.5 || longitude < 124.0 || longitude > 132.0) {
      log.warn("한국 외 지역 좌표일 가능성 - longitude: {}, latitude: {}", longitude, latitude);
      throw new LocationCoordinateOutOfRangeException(latitude, longitude);
    }

    String uri = UriComponentsBuilder
        .fromPath("/v2/local/geo/coord2regioncode.json")
        .queryParam("x", longitude)
        .queryParam("y", latitude)
        .build()
        .toUriString();

    log.debug("Kakao API 요청 URL: {}{}", BASE_URL, uri);

    try {
      KakaoApiResponse response = restClientBuilder
          .baseUrl(BASE_URL)
          .defaultHeader("Authorization", "KakaoAK " + kakaoApiKey)
          .build()
          .get()
          .uri(uri)
          .retrieve()
          .body(KakaoApiResponse.class);

      if (response == null || response.documents() == null || response.documents().isEmpty()) {
        log.warn("Kakao API에서 빈 응답 - longitude: {}, latitude: {}", longitude, latitude);
        LocationApiException exception = new LocationApiException((Throwable)null);
        exception.addDetail("longitude", longitude);
        exception.addDetail("latitude", latitude);
        throw exception;
      }

      List<KakaoApiResponse.Document> result = response.documents().stream()
          .filter(doc -> "H".equals(doc.region_type()))
          .toList();

      return result;

    } catch (LocationCoordinateOutOfRangeException | LocationApiException e) {
      // 이미 처리된 예외는 그대로 전파
      throw e;
    } catch (Exception e) {
      log.error("Kakao API 호출 실패 - longitude: {}, latitude: {}, error: {}",
          longitude, latitude, e.getMessage(), e);
      throw new LocationApiException(latitude, longitude, e);
    }
  }
}
