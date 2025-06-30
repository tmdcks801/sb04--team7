package com.example.ootd.domain.weather.entity;

public enum WindStrength {
  WEAK,
  MODERATE,
  STRONG;

  public static WindStrength from(Double wsd) {
    if (wsd == null) {
      return WEAK;
    }
    if (wsd < 4.0) {
      return WEAK;
    }
    if (wsd < 9.0) {
      return MODERATE;
    }
    return STRONG;
  }
}