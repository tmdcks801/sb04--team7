package com.example.ootd.batch.mapper;

import com.example.ootd.domain.weather.api.WeatherApiResponse;
import com.example.ootd.domain.weather.entity.Humidity;
import com.example.ootd.domain.weather.entity.Precipitation;
import com.example.ootd.domain.weather.entity.PrecipitationType;
import com.example.ootd.domain.weather.entity.SkyStatus;
import com.example.ootd.domain.weather.entity.Temperature;
import com.example.ootd.domain.weather.entity.Weather;
import com.example.ootd.domain.weather.entity.WindSpeed;
import com.example.ootd.domain.weather.entity.WindStrength;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WeatherApiEntityMapper {

  // 필요한 카테고리 정의 (필터링용으로만 사용)
  private static final Set<String> REQUIRED_CATEGORIES = Set.of(
      "POP", "PTY", "PCP", "SKY", "TMP", "TMN", "TMX", "REH", "WSD"
  );

  public Weather toWeather(List<WeatherApiResponse.Item> items, String regionName) {

    if (items == null || items.isEmpty()) {
      log.warn("No weather items for region: {}", regionName);
      return null;
    }

    Map<String, String> categoryMap = items.stream()
        .filter(item -> REQUIRED_CATEGORIES.contains(item.category()))
        .collect(Collectors.toMap(
            WeatherApiResponse.Item::category,
            WeatherApiResponse.Item::fcstValue,
            (existing, replacement) -> replacement
        ));

    // 날짜 시간 기준점 잡기
    WeatherApiResponse.Item first = items.get(0);
    String fcstDate = first.fcstDate();
    String fcstTime = first.fcstTime();
    String baseDate = first.baseDate();
    String baseTime = first.baseTime();

    try {
      // forecastAt: 예보 시각 (fcstDate + fcstTime)
      LocalDateTime forecastAt = LocalDateTime.of(
          LocalDate.parse(fcstDate, DateTimeFormatter.BASIC_ISO_DATE),
          LocalTime.of(Integer.parseInt(fcstTime) / 100, 0)
      );

      // forecastedAt: 예보 발표 시각 (baseDate + baseTime)
      LocalDateTime forecastedAt = LocalDateTime.of(
          LocalDate.parse(baseDate, DateTimeFormatter.BASIC_ISO_DATE),
          LocalTime.of(Integer.parseInt(baseTime) / 100, 0)
      );

      // 값 파싱
      Double currentTemp = parseDouble(categoryMap.get("TMP"));

      // 현재 온도가 없으면 처리 중단 (필수값)
      if (currentTemp == null) {
        log.warn("Current temperature is null for region: {}", regionName);
        return null;
      }

      // Temperature 객체 생성
      Temperature temperature = Temperature.builder()
          .temperatureCurrent(currentTemp)
          .temperatureMin(parseDoubleOrNull(categoryMap.get("TMN")))  // 있으면 사용, 없으면 null
          .temperatureMax(parseDoubleOrNull(categoryMap.get("TMX")))  // 있으면 사용, 없으면 null
          .build();

      // Humidity 객체 생성 (기본값 50.0)
      Humidity humidity = Humidity.builder()
          .humidityCurrent(parseDouble(categoryMap.getOrDefault("REH", "0.0")))
          .build();

      // Precipitation 객체 생성
      Precipitation precipitation = Precipitation.builder()
          .precipitationAmount(parsePrecipitationAmount(categoryMap.get("PCP")))
          .precipitationProbability(parseDouble(categoryMap.getOrDefault("POP", "0.0")))
          .precipitationType(parsePrecipitationType(categoryMap.get("PTY")))
          .build();

      // WindSpeed 객체 생성 (기본값 0.0)
      Double windSpeedVal = parseDouble(categoryMap.getOrDefault("WSD", "0.0"));
      WindSpeed windSpeed = WindSpeed.builder()
          .windSpeed(windSpeedVal)
          .windAsWord(WindStrength.from(windSpeedVal))
          .build();

      // Weather 객체 생성
      return Weather.builder()
          .id(UUID.randomUUID())
          .regionName(regionName)
          .forecastedAt(forecastedAt)
          .forecastAt(forecastAt)
          .temperature(temperature)
          .humidity(humidity)
          .precipitation(precipitation)
          .windSpeed(windSpeed)
          .skyStatus(parseSkyStatus(categoryMap.get("SKY")))
          .build();

    } catch (Exception e) {
      log.error("Error mapping weather data for region: {}", regionName, e);
      return null;
    }
  }

  private Double parseDouble(String val) {
    if (val == null || val.isBlank()) {
      return 0.0;  // null 대신 기본값 0.0 반환
    }
    try {
      String cleanValue = val.trim().replaceAll("[^0-9.-]", "");
      if (cleanValue.isEmpty()) {
        return 0.0;
      }
      return Double.parseDouble(cleanValue);
    } catch (NumberFormatException e) {
      log.debug("Failed to parse double from '{}': {}", val, e.getMessage());
      return 0.0;
    }
  }

  // TMN/TMX를 위한 null 가능한 parseDouble 메서드
  private Double parseDoubleOrNull(String val) {
    if (val == null || val.isBlank()) {
      return null;
    }
    try {
      String cleanValue = val.trim().replaceAll("[^0-9.-]", "");
      if (cleanValue.isEmpty()) {
        return null;
      }
      Double result = Double.parseDouble(cleanValue);
      // 0.0이면 null로 캘소 (의미없는 데이터)
      return result == 0.0 ? null : result;
    } catch (NumberFormatException e) {
      log.debug("Failed to parse double from '{}': {}", val, e.getMessage());
      return null;
    }
  }


  // api 응답에 없음을 한글로 적어놓아서 값으로 변경
  private Double parsePrecipitationAmount(String val) {
    if (val == null || val.isBlank()) {
      return 0.0;
    }
    if (val.contains("강수없음") || val.contains("없음")) {
      return 0.0;
    }
    if (val.contains("미만")) {
      return 0.1;
    }
    return parseDouble(val) != null ? parseDouble(val) : 0.0;
  }

  private PrecipitationType parsePrecipitationType(String code) {
    if (code == null) {
      return PrecipitationType.NONE;
    }

    return switch (code) {
      case "0" -> PrecipitationType.NONE;
      case "1" -> PrecipitationType.RAIN;
      case "2" -> PrecipitationType.RAIN_SNOW;
      case "3" -> PrecipitationType.SNOW;
      case "4" -> PrecipitationType.SHOWER;
      default -> {
        log.debug("Unknown precipitation type code: {}", code);
        yield PrecipitationType.NONE;
      }
    };
  }

  private SkyStatus parseSkyStatus(String code) {
    if (code == null) {
      return SkyStatus.CLOUDY;
    }

    return switch (code) {
      case "1" -> SkyStatus.CLEAR;
      case "3" -> SkyStatus.MOSTLY_CLOUDY;
      case "4" -> SkyStatus.CLOUDY;
      default -> {
        log.debug("Unknown sky status code: {}", code);
        yield SkyStatus.CLOUDY;
      }
    };
  }
}