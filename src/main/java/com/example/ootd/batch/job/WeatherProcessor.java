package com.example.ootd.batch.job;

import com.example.ootd.batch.dto.WeatherBatchData;
import com.example.ootd.batch.mapper.WeatherApiEntityMapper;
import com.example.ootd.domain.weather.entity.Weather;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class WeatherProcessor implements ItemProcessor<WeatherBatchData, Weather> {

  private final WeatherApiEntityMapper weatherApiEntityMapper;

  @Override
  public Weather process(WeatherBatchData data) {
    try {
      // Weather 엔티티로 변환
      Weather weather = weatherApiEntityMapper.toWeather(data.getItems(), data.getRegionName());

      if (weather == null) {
        log.warn("Weather mapping returned null for region: {}", data.getRegionName());
        return null;
      }

      // 전날 비교는 나중에 별도 Step이나 Service에서 처리
      // 일단 0.0으로 초기화
      weather.getTemperature().setTemperatureComparedToDayBefore(0.0);
      weather.getHumidity().setHumidityComparedToDayBefore(0.0);

      return weather;

    } catch (Exception e) {
      log.error("Failed to process weather data for region: {}", data.getRegionName(), e);
      return null;
    }
  }
}
