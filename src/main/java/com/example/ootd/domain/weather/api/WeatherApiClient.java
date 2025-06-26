package com.example.ootd.domain.weather.api;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
public class WeatherApiClient {

  private final RestClient.Builder restClientBuilder;

  @Value("${weather.api.key}")
  private String weatherApiKey;

  private static final String BASE_URL = "http://apis.data.go.kr";

  public List<WeatherApiResponse.Item> getVillageForecast(int nx, int ny) {
    LocalDate baseDate = LocalDate.now();
    String baseTime = "0500";

    String uri = UriComponentsBuilder
        .fromPath("/1360000/VilageFcstInfoService_2.0/getVilageFcst")
        .queryParam("serviceKey", weatherApiKey)
        .queryParam("numOfRows", 100)
        .queryParam("pageNo", 1)
        .queryParam("dataType", "JSON")
        .queryParam("base_date", baseDate.format(DateTimeFormatter.BASIC_ISO_DATE))
        .queryParam("base_time", baseTime)
        .queryParam("nx", nx)
        .queryParam("ny", ny)
        .build()
        .toUriString();

    WeatherApiResponse response = restClientBuilder
        .baseUrl(BASE_URL)
        .build()
        .get()
        .uri(uri)
        .retrieve()
        .body(WeatherApiResponse.class);

    return response.response().body().items().item();
  }
}
