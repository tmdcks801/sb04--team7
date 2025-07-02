package com.example.ootd.batch.job;

import com.example.ootd.domain.weather.entity.Weather;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class WeatherWriter implements ItemWriter<Weather> {

  private final JdbcTemplate jdbcTemplate;

  @Override
  public void write(@NonNull Chunk<? extends Weather> items) throws Exception {
    log.debug("Writing {} weather items", items.size());

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
    for (Weather weather : items) {
      try {
        int result = jdbcTemplate.update(sql,
            weather.getId(),
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

      } catch (Exception e) {
        log.error("Failed to save weather for region: {}", weather.getRegionName(), e);
        // 에러가 발생해도 다음 아이템 처리를 계속 진행
      }
    }

  }
}