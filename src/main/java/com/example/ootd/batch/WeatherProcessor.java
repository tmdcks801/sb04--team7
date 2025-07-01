package com.example.ootd.batch;

import com.example.ootd.domain.weather.entity.Weather;
import com.example.ootd.domain.weather.repository.WeatherRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class WeatherProcessor implements ItemProcessor<WeatherBatchData, Weather> {

  private final WeatherRepository weatherRepository;

  @Override
  public Weather process(WeatherBatchData data) {
    try {
      // Weather 엔티티로 변환
      Weather weather = WeatherApiEntityMapper.toWeather(data.getItems(), data.getRegionName());

      // 전날 같은 시간대 데이터 조회
      LocalDateTime yesterdaySameTime = weather.getForecastAt().minusDays(1);
      weatherRepository.findByRegionNameAndForecastAt(weather.getRegionName(), yesterdaySameTime)
          .ifPresentOrElse(yesterday -> {
            // 전날과 비교해서 차이 계산
            weather.getTemperature().setTemperatureComparedToDayBefore(
                weather.getTemperature().getTemperatureCurrent() - yesterday.getTemperature()
                    .getTemperatureCurrent()
            );
            weather.getHumidity().setHumidityComparedToDayBefore(
                weather.getHumidity().getHumidityCurrent() - yesterday.getHumidity()
                    .getHumidityCurrent()
            );
          }, () -> {
            // 전날 데이터가 없으면 0.0으로 설정
            log.warn(
                "No weather data found for comparison: region={}, forecastAt={}. Setting compared values to 0.0",
                weather.getRegionName(), yesterdaySameTime);

            weather.getTemperature().setTemperatureComparedToDayBefore(0.0);
            weather.getHumidity().setHumidityComparedToDayBefore(0.0);
          });

      return weather;

    } catch (Exception e) {
      log.error("Failed to process weather data for region: {}", data.getRegionName(), e);
      return null;
    }
  }
}