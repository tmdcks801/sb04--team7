package com.example.ootd.domain.weather.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Weather")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Weather {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id;
  private String regionName;

  private LocalDateTime forecastedAt;
  private LocalDateTime forecastAt;

  @Enumerated(EnumType.STRING)
  private SkyStatus skyStatus;

  @Embedded
  private Precipitation precipitation;

  @Embedded
  private Temperature temperature;

  @Embedded
  private Humidity humidity;

  @Embedded
  private WindSpeed windSpeed;

}
