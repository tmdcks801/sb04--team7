package com.example.ootd.domain.weather.repository;

import com.example.ootd.domain.weather.entity.Weather;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WeatherRepository extends JpaRepository<Weather, UUID> {

  // 지역별 최신 예보 데이터 중 자정 시간대 데이터 조회
  @Query("""
      SELECT w FROM Weather w
      WHERE w.regionName = :regionName
        AND w.forecastedAt = (
          SELECT MAX(w2.forecastedAt)
          FROM Weather w2
          WHERE w2.regionName = :regionName
        )
        AND EXTRACT(HOUR FROM w.forecastAt) = 0
        AND EXTRACT(MINUTE FROM w.forecastAt) = 0
      """)
  List<Weather> findMidnightWeathersByRegionNameWithLatestForecastedAt(String regionName);

  // 지역별 최신 예보 데이터 조회 (오늘/내일/모래 날씨용)
  @Query("""
      SELECT w FROM Weather w
      WHERE w.regionName = :regionName
        AND w.forecastedAt = (
          SELECT MAX(w2.forecastedAt)
          FROM Weather w2
          WHERE w2.regionName = :regionName
        )
        AND w.forecastAt >= :startDate
        AND w.forecastAt <= :endDate
      ORDER BY w.forecastAt ASC
      """)
  List<Weather> findWeathersByRegionAndDateRange(
      @Param("regionName") String regionName,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate
  );

  // 특정 지역과 예보 시간으로 조회
  Optional<Weather> findByRegionNameAndForecastAt(String regionName, LocalDateTime forecastAt);

  @Modifying
  @Query("""
      DELETE FROM Weather w 
      WHERE w.forecastedAt < :cutoffDate 
        AND w.id NOT IN (
            SELECT DISTINCT f.weather.id 
            FROM Feed f 
            WHERE f.weather IS NOT NULL
        )
      """)
  int deleteOldWeatherDataPreservingFeedReferences(@Param("cutoffDate") LocalDateTime cutoffDate);

  @Query(value = """
    SELECT * FROM weather w 
    WHERE w.forecast_at::date = CURRENT_DATE
    AND w.region_name = (
        SELECT l.location_names FROM locations l 
        JOIN users u ON u.location_id = l.id 
        WHERE u.id = :userId
        LIMIT 1
    )
    ORDER BY w.forecast_at
    """, nativeQuery = true)
  List<Weather> findTodayWeatherByUserLocation(@Param("userId") UUID userId);
}
