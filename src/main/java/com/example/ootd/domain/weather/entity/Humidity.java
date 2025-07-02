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
public class Humidity {

  private Double humidityCurrent;
  private Double humidityComparedToDayBefore;

  public void setHumidityComparedToDayBefore(Double diff) {
    this.humidityComparedToDayBefore = diff;
  }
}
