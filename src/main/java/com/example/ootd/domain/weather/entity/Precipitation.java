package com.example.ootd.domain.weather.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

@Embeddable
@Getter
public class Precipitation {

  @Enumerated(EnumType.STRING)
  private PrecipitationType type;

  private double amount;
  private double probability;
}