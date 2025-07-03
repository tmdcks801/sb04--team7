package com.example.ootd.batch.job;

import com.example.ootd.domain.weather.entity.Weather;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class WeatherWriter implements ItemWriter<Weather> {

  private final JdbcTemplate jdbcTemplate;

  @Override
  @Transactional
  public void write(@NonNull Chunk<? extends Weather> items) throws Exception {
    if (items.isEmpty()) {
      return;
    }

    log.debug("Writing {} weather items using batch insert", items.size());

    List<Weather> weatherList = new ArrayList<>(items.getItems());

    try {
      batchInsert(weatherList);
      log.info("Successfully saved {} weather records", weatherList.size());

    } catch (Exception e) {
      log.error("Batch insert failed, falling back to individual inserts", e);
      fallbackToIndividualInserts(weatherList);
    }
  }

  private void batchInsert(List<Weather> weatherList) {
    String sql = """
        INSERT INTO weather (
            id, region_name, forecasted_at, forecast_at, 
            sky_status, precipitation_amount, precipitation_probability, 
            precipitation_type, temperature_current, temperature_min, 
            temperature_max, temperature_compared_to_day_before,
            humidity_current, humidity_compared_to_day_before,
            wind_speed, wind_as_word
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps, int i) throws SQLException {
        Weather weather = weatherList.get(i);

        ps.setObject(1, weather.getId(), java.sql.Types.OTHER);
        ps.setString(2, weather.getRegionName());
        ps.setTimestamp(3, java.sql.Timestamp.valueOf(weather.getForecastedAt()));
        ps.setTimestamp(4, java.sql.Timestamp.valueOf(weather.getForecastAt()));
        ps.setString(5, weather.getSkyStatus().name());
        setDoubleOrNull(ps, 6, weather.getPrecipitation().getPrecipitationAmount());
        setDoubleOrNull(ps, 7, weather.getPrecipitation().getPrecipitationProbability());
        ps.setString(8, weather.getPrecipitation().getPrecipitationType().name());
        setDoubleOrNull(ps, 9, weather.getTemperature().getTemperatureCurrent());
        setDoubleOrNull(ps, 10, weather.getTemperature().getTemperatureMin());
        setDoubleOrNull(ps, 11, weather.getTemperature().getTemperatureMax());
        setDoubleOrNull(ps, 12, weather.getTemperature().getTemperatureComparedToDayBefore());
        setDoubleOrNull(ps, 13, weather.getHumidity().getHumidityCurrent());
        setDoubleOrNull(ps, 14, weather.getHumidity().getHumidityComparedToDayBefore());
        setDoubleOrNull(ps, 15, weather.getWindSpeed().getWindSpeed());
        ps.setString(16, weather.getWindSpeed().getWindAsWord().name());
      }

      @Override
      public int getBatchSize() {
        return weatherList.size();
      }
    });
  }

  //Double 값 또는 null을 안전하게 설정
  private void setDoubleOrNull(PreparedStatement ps, int parameterIndex, Double value)
      throws SQLException {
    if (value != null) {
      ps.setDouble(parameterIndex, value);
    } else {
      ps.setNull(parameterIndex, java.sql.Types.DOUBLE);
    }
  }

  //배치 삽입 실패 시 개별 삽입 폴백
  private void fallbackToIndividualInserts(List<Weather> weatherList) {
    String sql = """
        INSERT INTO weather (
            id, region_name, forecasted_at, forecast_at, 
            sky_status, precipitation_amount, precipitation_probability, 
            precipitation_type, temperature_current, temperature_min, 
            temperature_max, temperature_compared_to_day_before,
            humidity_current, humidity_compared_to_day_before,
            wind_speed, wind_as_word
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    int successCount = 0;
    for (Weather weather : weatherList) {
      try {
        int result = jdbcTemplate.update(sql,
            weather.getId(),  // UUID 그대로 사용 (JdbcTemplate이 자동 변환)
            weather.getRegionName(),
            weather.getForecastedAt(),
            weather.getForecastAt(),
            weather.getSkyStatus().name(),
            weather.getPrecipitation().getPrecipitationAmount(),
            weather.getPrecipitation().getPrecipitationProbability(),
            weather.getPrecipitation().getPrecipitationType().name(),
            weather.getTemperature().getTemperatureCurrent(),
            weather.getTemperature().getTemperatureMin(),
            weather.getTemperature().getTemperatureMax(),
            weather.getTemperature().getTemperatureComparedToDayBefore(),
            weather.getHumidity().getHumidityCurrent(),
            weather.getHumidity().getHumidityComparedToDayBefore(),
            weather.getWindSpeed().getWindSpeed(),
            weather.getWindSpeed().getWindAsWord().name()
        );
        if (result > 0) {
          successCount++;
        }
      } catch (Exception e) {
        log.error("Failed to save weather for region: {}", weather.getRegionName(), e);
      }
    }
    log.info("Individual insert completed: {}/{} successful", successCount, weatherList.size());
  }
}