package com.example.ootd.domain.weather.api;

import com.example.ootd.exception.weather.WeatherApiException;
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

    return callWeatherApi(nx, ny, numOfRows, baseDateTime.date(), baseDateTime.time());
  }

  //TMN/TMX 가져오는 메서드어제 02:00 기준으로 48시간 데이터를 가져와서 오늘 날짜의 TMN/TMX를 찾음
  public List<WeatherApiResponse.Item> getTemperatureMinMax(int nx, int ny) {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    String baseDate = yesterday.format(DateTimeFormatter.BASIC_ISO_DATE);
    String baseTime = "0200";
    int numOfRows = 600; // 48시간 데이터

    log.debug("TMN/TMX 데이터 요청 - baseDate: {}, baseTime: {}, numOfRows: {}",
        baseDate, baseTime, numOfRows);

    return callWeatherApi(nx, ny, numOfRows, baseDate, baseTime);
  }

  // 공통 날씨 API 호출 메서드
  private List<WeatherApiResponse.Item> callWeatherApi(int nx, int ny, int numOfRows,
      String baseDate, String baseTime) {
    String fullUrl = UriComponentsBuilder
        .fromHttpUrl(FULL_BASE_URL)
        .queryParam("serviceKey", weatherApiKey)
        .queryParam("numOfRows", numOfRows)
        .queryParam("pageNo", 1)
        .queryParam("dataType", "JSON")
        .queryParam("base_date", baseDate)
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
        WeatherApiException exception = new WeatherApiException((Throwable) null);
        exception.addDetail("responseEmpty", true);
        throw exception;
      }

      // API 응답 상태 확인
      if (!"00".equals(response.response().header().resultCode())) {
        WeatherApiException exception = new WeatherApiException((Throwable) null);
        exception.addDetail("resultCode", response.response().header().resultCode());
        exception.addDetail("resultMsg", response.response().header().resultMsg());
        throw exception;
      }

      List<WeatherApiResponse.Item> items = response.response().body().items().item();
      log.debug("API 응답 데이터 개수: {}", items != null ? items.size() : 0);

      return items;

    } catch (WeatherApiException e) {
      // 이미 처리된 날씨 API 예외는 그대로 전파
      throw e;
    } catch (Exception e) {
      log.error("날씨 API 호출 실패 - baseDate: {}, baseTime: {}", baseDate, baseTime, e);
      throw new WeatherApiException(e);
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