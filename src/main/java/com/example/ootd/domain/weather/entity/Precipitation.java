package com.example.ootd.domain.weather.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

@Embeddable
@Getter
public class Precipitation {

  @Enumerated(EnumType.STRING)
  private PrecipitationType precipitationType;
  private double precipitationAmount;
  private double precipitationProbability;
}