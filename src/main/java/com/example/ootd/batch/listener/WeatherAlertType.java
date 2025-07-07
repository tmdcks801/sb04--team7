package com.example.ootd.batch.listener;

import lombok.Getter;

// 날씨 알림 유형 정의
@Getter
public enum WeatherAlertType {
  HEAT_WAVE("폭염 경보"),
  HEAVY_RAIN("호우 경보"),
  SNOW_WARNING("대설 경보"),
  STRONG_WIND("강풍 경보");

  private final String message;

  WeatherAlertType(String message) {
    this.message = message;
  }

}