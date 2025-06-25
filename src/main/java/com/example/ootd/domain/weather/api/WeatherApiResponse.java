package com.example.ootd.domain.weather.api;

import java.util.List;

public record WeatherApiResponse(Response response) {

  public record Response(
      Body body
  ) {

  }

  public record Body(
      Items items
  ) {

  }

  public record Items(
      List<Item> item
  ) {

  }

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
