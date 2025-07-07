package com.example.ootd.exception.weather;

import com.example.ootd.exception.ErrorCode;

// 날씨 데이터 부족 시
public class WeatherDataInsufficientException extends WeatherException {

  public WeatherDataInsufficientException(int expectedDays, int actualDays) {
    super(ErrorCode.WEATHER_DATA_INSUFFICIENT);
    addDetail("expectedDays", expectedDays);
    addDetail("actualDays", actualDays);
  }
}
