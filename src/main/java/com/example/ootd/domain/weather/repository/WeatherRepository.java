package com.example.ootd.domain.weather.repository;

import com.example.ootd.domain.weather.entity.Weather;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface WeatherRepository extends JpaRepository<Weather, UUID> {

  Optional<Weather> findFirstByRegionNameOrderByForecastedAtDesc(String regionName);
}
