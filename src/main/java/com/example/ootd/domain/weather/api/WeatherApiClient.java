package com.example.ootd.domain.weather.api;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

  private static final List<String> BASE_TIMES = List.of(
      "0200", "0500", "0800", "1100", "1400", "1700", "2000", "2300"
  );

  private static final String FULL_BASE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";

  public List<WeatherApiResponse.Item> getVillageForecast(int nx, int ny, int numOfRows) {
    LocalDateTime now = LocalDateTime.now();

    // 현재 시각 기준으로 가장 최근 발표 시각 계산
    BaseDateTime baseDateTime = calculateBaseDateTime(now);

    String fullUrl = UriComponentsBuilder
        .fromHttpUrl(FULL_BASE_URL)
        .queryParam("serviceKey", weatherApiKey)
        .queryParam("numOfRows", numOfRows)
        .queryParam("pageNo", 1)
        .queryParam("dataType", "JSON")
        .queryParam("base_date", baseDateTime.date())
        .queryParam("base_time", baseDateTime.time())
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

  private BaseDateTime calculateBaseDateTime(LocalDateTime now) {
    LocalTime currentTime = now.toLocalTime();

    // 현재 시각보다 10분 전 기준으로 계산 (API 제공 시간 고려)
    LocalTime adjustedTime = currentTime.minusMinutes(10);

    // 가장 최근 발표 시각 찾기
    String baseTime = BASE_TIMES.stream()
        .filter(time -> {
          LocalTime apiTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HHmm"));
          return apiTime.isBefore(adjustedTime) || apiTime.equals(adjustedTime);
        })
        .reduce((first, second) -> second)
        .orElse("2300"); // 모든 시간이 현재보다 늦으면 전날 23시

    // 날짜 조정 (00:00 ~ 02:10 사이면 전날 날짜 사용)
    LocalDate baseDate = now.toLocalDate();
    if (baseTime.equals("2300") && currentTime.isBefore(LocalTime.of(2, 10))) {
      baseDate = baseDate.minusDays(1);
    }

    return new BaseDateTime(
        baseDate.format(DateTimeFormatter.BASIC_ISO_DATE),
        baseTime
    );
  }

  private record BaseDateTime(String date, String time) {

  }
}