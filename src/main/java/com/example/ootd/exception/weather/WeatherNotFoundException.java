package com.example.ootd.exception.weather;

import com.example.ootd.exception.ErrorCode;
import java.util.UUID;

// 날씨 정보를 찾을 수 없을 때
public class WeatherNotFoundException extends WeatherException {

  public WeatherNotFoundException() {
    super(ErrorCode.WEATHER_NOT_FOUND);
  }

  public WeatherNotFoundException(String regionName, String dateRange) {
    super(ErrorCode.WEATHER_NOT_FOUND);
    addDetail("regionName", regionName);
    addDetail("dateRange", dateRange);
  }

  public static WeatherNotFoundException withId(UUID weatherId) {
    WeatherNotFoundException exception = new WeatherNotFoundException();
    exception.addDetail("weatherId", weatherId);
    return exception;
  }
}
