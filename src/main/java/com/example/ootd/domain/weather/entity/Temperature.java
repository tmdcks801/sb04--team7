package com.example.ootd.domain.weather.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Temperature {

  private Double temperatureCurrent;
  private Double temperatureMin;
  private Double temperatureMax;
  private Double temperatureComparedToDayBefore;

  public void setTemperatureCurrent(Double temperatureCurrent) {
    this.temperatureCurrent = temperatureCurrent;
  }

  public void setTemperatureMin(Double temperatureMin) {
    this.temperatureMin = temperatureMin;
  }

  public void setTemperatureMax(Double temperatureMax) {
    this.temperatureMax = temperatureMax;
  }

  public void setTemperatureComparedToDayBefore(Double diff) {
    this.temperatureComparedToDayBefore = diff;
  }
}


