package com.example.ootd.domain.weather.repository;

import com.example.ootd.domain.weather.entity.Weather;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WeatherRepository extends JpaRepository<Weather, UUID> {

  @Query("""
      SELECT w FROM Weather w
      WHERE w.regionName = :regionName
        AND w.forecastedAt = (
          SELECT MAX(w2.forecastedAt)
          FROM Weather w2
          WHERE w2.regionName = :regionName
        )
        AND FUNCTION('HOUR', w.forecastAt) = 0
        AND FUNCTION('MINUTE', w.forecastAt) = 0
      """)
  List<Weather> findMidnightWeathersByRegionNameWithLatestForecastedAt(String regionName);

  Optional<Weather> findByRegionNameAndForecastAt(String regionName, LocalDateTime forecastAt);
}
