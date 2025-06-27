package com.example.ootd.domain.weather.api;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherApiClient {

  private final RestClient.Builder restClientBuilder;

  @Value("${weather.api.key}")
  private String weatherApiKey;

  private static final String FULL_BASE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";

  public List<WeatherApiResponse.Item> getVillageForecast(int nx, int ny) {
    LocalDate baseDate = LocalDate.now();
    String baseTime = "0500";

    String fullUrl = UriComponentsBuilder
        .fromHttpUrl(FULL_BASE_URL)
        .queryParam("serviceKey", weatherApiKey)
        .queryParam("numOfRows", 100)
        .queryParam("pageNo", 1)
        .queryParam("dataType", "JSON")
        .queryParam("base_date", baseDate.format(DateTimeFormatter.BASIC_ISO_DATE))
        .queryParam("base_time", baseTime)
        .queryParam("nx", nx)
        .queryParam("ny", ny)
        .build(false)
        .toUriString();

    log.debug("API 요청 URL: {}", fullUrl);

    try {
      URI uri = new URI(fullUrl);

      WeatherApiResponse response = restClientBuilder
          .build()
          .get()
          .uri(uri)
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(WeatherApiResponse.class);

      if (response == null || response.response() == null
          || response.response().body() == null
          || response.response().body().items() == null) {
        throw new RuntimeException("날씨 API 응답이 비어있습니다.");
      }

      // API 응답 상태 확인
      if (!"00".equals(response.response().header().resultCode())) {
        throw new RuntimeException("날씨 API 오류: " + response.response().header().resultMsg());
      }

      return response.response().body().items().item();

    } catch (Exception e) {
      log.error("날씨 API 호출 실패", e);
      throw new RuntimeException("날씨 정보를 가져올 수 없습니다.", e);
    }
  }
}