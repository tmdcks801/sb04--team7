package com.example.ootd.domain.weather.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherApiResponse(Response response) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Response(
      Header header,
      Body body
  ) {

  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Header(
      String resultCode,
      String resultMsg
  ) {

  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Body(
      String dataType,
      Items items
  ) {

  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Items(
      List<Item> item
  ) {

  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Item(
      String baseDate,
      String baseTime,
      String category,
      String fcstDate,
      String fcstTime,
      String fcstValue,
      int nx,
      int ny
  ) {

  }
}