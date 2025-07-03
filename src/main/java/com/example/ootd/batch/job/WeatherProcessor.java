package com.example.ootd.batch.job;

import com.example.ootd.batch.dto.WeatherBatchData;
import com.example.ootd.batch.mapper.WeatherApiEntityMapper;
import com.example.ootd.domain.weather.api.WeatherApiResponse;
import com.example.ootd.domain.weather.entity.Weather;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class WeatherProcessor implements ItemProcessor<WeatherBatchData, Weather> {

  private final WeatherApiEntityMapper weatherApiEntityMapper;

  // TMN/TMX 로깅 중복 방지를 위한 지역별 캐시
  private static final Set<String> processedRegions = ConcurrentHashMap.newKeySet();

  @Override
  public Weather process(WeatherBatchData data) {
    try {
      // 기본 Weather 엔티티로 변환
      Weather weather = weatherApiEntityMapper.toWeather(data.getItems(), data.getRegionName());

      if (weather == null) {
        log.warn("Weather mapping returned null for region: {}", data.getRegionName());
        return null;
      }

      // TMN/TMX 데이터 보완 (추가 API 호출 없이 previousItems에서 찾기)
      enhanceTemperatureMinMax(weather, data);

      // 전날 같은 시간대 비교 계산
      calculateComparison(weather, data);

      return weather;

    } catch (Exception e) {
      log.error("Failed to process weather data for region: {}", data.getRegionName(), e);
      return null;
    }
  }

  // previousItems에서 TMN/TMX 데이터를 찾아서 보완
  private void enhanceTemperatureMinMax(Weather weather, WeatherBatchData data) {
    try {
      // 이미 TMN, TMX 데이터가 모두 있으면 스킵
      if (weather.getTemperature().getTemperatureMin() != null &&
          weather.getTemperature().getTemperatureMax() != null) {
        log.debug("TMN/TMX data already available for region: {}", data.getRegionName());
        return;
      }

      List<WeatherApiResponse.Item> previousItems = data.getPreviousItems();
      if (previousItems == null || previousItems.isEmpty()) {
        log.warn("No previous items available for TMN/TMX enhancement: {}", data.getRegionName());
        return;
      }

      // 오늘 날짜의 TMN/TMX 데이터 찾기
      String todayDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

      Map<String, String> todayTempMap = previousItems.stream()
          .filter(item -> todayDate.equals(item.fcstDate()))
          .filter(item -> "TMN".equals(item.category()) || "TMX".equals(item.category()))
          .collect(Collectors.toMap(
              WeatherApiResponse.Item::category,
              WeatherApiResponse.Item::fcstValue,
              (existing, replacement) -> replacement
          ));

      // TMN/TMX 데이터 업데이트
      Double temperatureMin = parseDouble(todayTempMap.get("TMN"));
      Double temperatureMax = parseDouble(todayTempMap.get("TMX"));

      // 데이터 설정 (모든 시간대에서 수행)
      if (temperatureMin != null) {
        weather.getTemperature().setTemperatureMin(temperatureMin);
      }
      if (temperatureMax != null) {
        weather.getTemperature().setTemperatureMax(temperatureMax);
      }

      // 로그는 지역별로 한 번만 출력 (중복 방지)
      String regionKey = data.getRegionName();
      if (!processedRegions.contains(regionKey) && (temperatureMin != null
          || temperatureMax != null)) {
        processedRegions.add(regionKey);
      }

    } catch (Exception e) {
      log.error("Failed to enhance TMN/TMX data for region: {}", data.getRegionName(), e);
    }
  }

  // 전날 같은 시간대와 비교하여 차이값 계산
  private void calculateComparison(Weather weather, WeatherBatchData data) {
    try {
      List<WeatherApiResponse.Item> previousItems = data.getPreviousItems();
      if (previousItems == null || previousItems.isEmpty()) {
        log.debug("No previous items for comparison: {}", data.getRegionName());
        weather.getTemperature().setTemperatureComparedToDayBefore(0.0);
        weather.getHumidity().setHumidityComparedToDayBefore(0.0);
        return;
      }

      LocalDateTime forecastAt = weather.getForecastAt();
      LocalDateTime yesterdayAt = forecastAt.minusDays(1);

      String yesterdayDate = yesterdayAt.format(DateTimeFormatter.BASIC_ISO_DATE);
      String yesterdayTime = String.format("%04d", yesterdayAt.getHour() * 100);

      // 전날 같은 시간대의 데이터 찾기
      Map<String, String> yesterdayDataMap = previousItems.stream()
          .filter(item -> yesterdayDate.equals(item.fcstDate()) && yesterdayTime.equals(
              item.fcstTime()))
          .filter(item -> "TMP".equals(item.category()) || "REH".equals(item.category()))
          .collect(Collectors.toMap(
              WeatherApiResponse.Item::category,
              WeatherApiResponse.Item::fcstValue,
              (existing, replacement) -> replacement
          ));

      // 전날 데이터와 비교
      Double yesterdayTemp = parseDouble(yesterdayDataMap.get("TMP"));
      Double yesterdayHumidity = parseDouble(yesterdayDataMap.get("REH"));

      if (yesterdayTemp != null) {
        double tempDiff = weather.getTemperature().getTemperatureCurrent() - yesterdayTemp;
        weather.getTemperature().setTemperatureComparedToDayBefore(tempDiff);
        log.debug("Temperature comparison for {}: current={}, yesterday={}, diff={}",
            data.getRegionName(), weather.getTemperature().getTemperatureCurrent(), yesterdayTemp,
            tempDiff);
      } else {
        weather.getTemperature().setTemperatureComparedToDayBefore(0.0);
      }

      if (yesterdayHumidity != null) {
        double humidityDiff = weather.getHumidity().getHumidityCurrent() - yesterdayHumidity;
        weather.getHumidity().setHumidityComparedToDayBefore(humidityDiff);
        log.debug("Humidity comparison for {}: current={}, yesterday={}, diff={}",
            data.getRegionName(), weather.getHumidity().getHumidityCurrent(), yesterdayHumidity,
            humidityDiff);
      } else {
        weather.getHumidity().setHumidityComparedToDayBefore(0.0);
      }

    } catch (Exception e) {
      log.error("Failed to calculate comparison for region: {}", data.getRegionName(), e);
      weather.getTemperature().setTemperatureComparedToDayBefore(0.0);
      weather.getHumidity().setHumidityComparedToDayBefore(0.0);
    }
  }

  // 문자열을 Double로 변환
  private Double parseDouble(String val) {
    if (val == null || val.isBlank()) {
      return null;
    }
    try {
      String cleanValue = val.trim().replaceAll("[^0-9.-]", "");
      if (cleanValue.isEmpty()) {
        return null;
      }
      return Double.parseDouble(cleanValue);
    } catch (NumberFormatException e) {
      log.debug("Failed to parse double from '{}': {}", val, e.getMessage());
      return null;
    }
  }
}
