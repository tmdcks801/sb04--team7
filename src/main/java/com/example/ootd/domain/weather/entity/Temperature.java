package com.example.ootd.domain.weather.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Temperature {

  private double current;
  private double min;
  private double max;
  private double comparedToDayBefore;
}

