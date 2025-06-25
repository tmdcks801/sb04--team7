package com.example.ootd.domain.weather.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Humidity {

  private double humidityCurrent;
  private double humidityComparedToDayBefore;
}
