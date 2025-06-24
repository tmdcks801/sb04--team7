package com.example.ootd.domain.weather.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

@Embeddable
@Getter
public class WindSpeed {

  private double speed;

  @Enumerated(EnumType.STRING)
  private WindStrength asWord;
}