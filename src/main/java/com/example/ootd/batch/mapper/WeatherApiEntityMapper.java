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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component  // Spring Bean으로 등록
public class WeatherApiEntityMapper {

  //필요한 카테고리 정의
  private static final Set<String> REQUIRED_CATEGORIES = Set.of(
      "POP", "PTY", "PCP", "SKY", "TMP", "TMN", "TMX", "REH", "WSD"
  );

  // WeatherApiResponse 에서 DB에 저장할 수 있게 형식 바꾸기
  public Weather toWeather(List<WeatherApiResponse.Item> items, String regionName) {

    // 아이템 비어있으면 null 반환 (Spring Batch가 skip 처리)
    if (items == null || items.isEmpty()) {
      log.warn("No weather items for region: {}", regionName);
      return null;
    }

    Map<String, String> categoryMap = items.stream()
        // 필요없는 카테고리 제거
        .filter(item -> REQUIRED_CATEGORIES.contains(item.category()))
        // 카테고리별로 값넣기
        .collect(Collectors.toMap(
            WeatherApiResponse.Item::category,
            WeatherApiResponse.Item::fcstValue,
            (existing, replacement) -> replacement
        ));

    //날짜 시간 기준점 잡기
    WeatherApiResponse.Item first = items.get(0);
    String fcstDate = first.fcstDate();
    String fcstTime = first.fcstTime();
    String baseTime = first.baseTime();

    // 카테고리 있는지 없는지 확인
    Set<String> requiredCategories = new HashSet<>(REQUIRED_CATEGORIES);

    // 0200이 아닌 경우 TMN, TMX는 필수가 아님
    if (!"0200".equals(baseTime)) {
      requiredCategories.remove("TMN");
      requiredCategories.remove("TMX");
    }

    // 필수 카테고리 체크 (TMN, TMX 제외)
    Set<String> essentialCategories = Set.of("TMP", "SKY", "PTY");
    for (String category : essentialCategories) {
      if (!categoryMap.containsKey(category)) {
        log.error("Missing essential category [{}] for region: {}", category, regionName);
        return null;  // 필수 데이터가 없으면 null 반환
      }
    }

    try {
      LocalDateTime forecastAt = LocalDateTime.of(
          LocalDate.parse(fcstDate, DateTimeFormatter.BASIC_ISO_DATE),
          LocalTime.of(Integer.parseInt(fcstTime) / 100, 0)
      );

      // 값 파싱
      Double windSpeedVal = parseDouble(categoryMap.get("WSD"));
      Double tmpMin = "0200".equals(baseTime) ? parseDouble(categoryMap.get("TMN")) : null;
      Double tmpMax = "0200".equals(baseTime) ? parseDouble(categoryMap.get("TMX")) : null;
      Double currentTemp = parseDouble(categoryMap.get("TMP"));

      // 현재 온도가 없으면 처리 중단
      if (currentTemp == null) {
        log.warn("Current temperature is null for region: {}", regionName);
        return null;
      }

      // Temperature 객체 생성 - 빌더 패턴 사용
      Temperature temperature = Temperature.builder()
          .temperatureCurrent(currentTemp)
          .temperatureMin(tmpMin)
          .temperatureMax(tmpMax)
          .build();

      // Humidity 객체 생성 - 빌더 패턴 사용
      Double humidityValue = parseDouble(categoryMap.get("REH"));
      Humidity humidity = Humidity.builder()
          .humidityCurrent(humidityValue != null ? humidityValue : 50.0)
          .build();

      // Precipitation 객체 생성 - 빌더 패턴 사용
      Double popValue = parseDouble(categoryMap.get("POP"));
      Precipitation precipitation = Precipitation.builder()
          .precipitationAmount(parsePrecipitationAmount(categoryMap.get("PCP")))
          .precipitationProbability(popValue != null ? popValue : 0.0)
          .precipitationType(parsePrecipitationType(categoryMap.get("PTY")))
          .build();

      // WindSpeed 객체 생성 - 빌더 패턴 사용
      if (windSpeedVal == null) {
        windSpeedVal = 0.0;
      }
      WindSpeed windSpeed = WindSpeed.builder()
          .windSpeed(windSpeedVal)
          .windAsWord(WindStrength.from(windSpeedVal))
          .build();

      // Weather 객체 생성
      return Weather.builder()
          .id(UUID.randomUUID())
          .regionName(regionName)
          .forecastedAt(LocalDateTime.now())
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

  // string으로 되어있는 데이터 double로 바꾸기
  private Double parseDouble(String val) {
    if (val == null || val.isBlank()) {
      return null;
    }
    try {
      // "mm" 같은 단위 제거
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