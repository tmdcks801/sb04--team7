package com.example.ootd.domain.weather.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Temperature {

  private double temperatureCurrent;
  private double temperatureMin;
  private double temperatureMax;
  private double temperatureComparedToDayBefore;
}


