package com.example.ootd.domain.weather.entity;

import com.example.ootd.domain.location.entity.Location;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Wheather")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wheather {

  @Id
  @GeneratedValue
  private UUID id;

  private LocalDateTime forecastedAt;
  private LocalDateTime forecastAt;


  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "location_id")
  private Location location;

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
