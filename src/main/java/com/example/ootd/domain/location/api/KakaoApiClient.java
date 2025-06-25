package com.example.ootd.domain.location.api;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

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
    String uri = UriComponentsBuilder
        .fromPath("/v2/local/geo/coord2regioncode.json")
        .queryParam("x", longitude)
        .queryParam("y", latitude)
        .build()
        .toUriString();

    KakaoApiResponse response = restClientBuilder
        .baseUrl(BASE_URL)
        .defaultHeader("Authorization", "KakaoAK " + kakaoApiKey)
        .build()
        .get()
        .uri(uri)
        .retrieve()
        .body(KakaoApiResponse.class);

    return response.documents().stream()
        .filter(doc -> "H".equals(doc.region_type()))
        .toList();
  }
}
