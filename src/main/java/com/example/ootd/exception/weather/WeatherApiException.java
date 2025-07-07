package com.example.ootd.exception.weather;

import com.example.ootd.exception.ErrorCode;

// 날씨 API 호출 실패 시
public class WeatherApiException extends WeatherException {

  public WeatherApiException(Throwable cause) {
    super(ErrorCode.WEATHER_API_ERROR, cause);
  }
}
