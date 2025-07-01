package com.example.ootd.batch;

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

@Slf4j
public class WeatherApiEntityMapper {

  //필요한 카테고리 정의
  private static final Set<String> REQUIRED_CATEGORIES = Set.of(
      "POP", "PTY", "PCP", "SKY", "TMP", "TMN", "TMX", "REH", "WSD"
  );

  // WeatherApiResponse 에서 DB에 저장할 수 있게 형식 바꾸기
  public static Weather toWeather(List<WeatherApiResponse.Item> items, String regionName) {

    // 아이템 비어있으면 오류 발생
    if (items == null || items.isEmpty()) {
      throw new IllegalArgumentException("items cannot be null or empty");
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

    if (!"0200".equals(first.baseTime())) {
      requiredCategories.remove("TMN");
      requiredCategories.remove("TMX");
    }

    for (String category : REQUIRED_CATEGORIES) {
      if (!categoryMap.containsKey(category)) {
        log.warn("Missing category [{}] in weather API response", category);
      }
    }

    //

    LocalDateTime forecastAt = LocalDateTime.of(
        LocalDate.parse(fcstDate, DateTimeFormatter.BASIC_ISO_DATE),
        LocalTime.of(Integer.parseInt(fcstTime) / 100, 0)
    );

    // 새벽 2시에만 tmn이랑 tmx 있어서 설정
    Double windSpeedVal = parseDouble(categoryMap.get("WSD"));
    Double tmpMin = "0200".equals(baseTime) ? parseDouble(categoryMap.get("TMN")) : null;
    Double tmpMax = "0200".equals(baseTime) ? parseDouble(categoryMap.get("TMX")) : null;

    // 최종적으로 DB에 저장할 수 있게 바꾸기
    return Weather.builder()
        .id(UUID.randomUUID())
        .regionName(regionName)
        .forecastedAt(LocalDateTime.now())
        .forecastAt(forecastAt)
        .temperature(Temperature.builder()
            .temperatureCurrent(parseDouble(categoryMap.get("TMP")))
            .temperatureMin(tmpMin)
            .temperatureMax(tmpMax)
            .build())
        .humidity(Humidity.builder()
            .humidityCurrent(parseDouble(categoryMap.get("REH")))
            .build())
        .precipitation(Precipitation.builder()
            .precipitationAmount(parsePrecipitationAmount(categoryMap.get("PCP")))
            .precipitationProbability(parseDouble(categoryMap.get("POP")))
            .precipitationType(parsePrecipitationType(categoryMap.get("PTY")))
            .build())
        .windSpeed(WindSpeed.builder()
            .windSpeed(windSpeedVal)
            .windAsWord(WindStrength.from(windSpeedVal))
            .build())
        .skyStatus(parseSkyStatus(categoryMap.get("SKY")))
        .build();
  }

  // string으로 되어있는 데이터 double로 바꾸기
  private static Double parseDouble(String val) {
    if (val == null || val.isBlank()) {
      return null;
    }
    try {
      return Double.parseDouble(val.trim()); // mm 제거 생략
    } catch (NumberFormatException e) {
      log.debug("Failed to parse double from '{}': {}", val, e.getMessage());
      return null;
    }
  }


  // api 응답에 없음을 한글로 적어놓아서 값으로 변경
  private static Double parsePrecipitationAmount(String val) {
    if (val == null || val.isBlank()) {
      return 0.0;
    }
    if (val.contains("강수없음") || val.contains("없음")) {
      return 0.0;
    }
    if (val.contains("미만")) {
      return 0.1;
    }
    return parseDouble(val);
  }

  private static PrecipitationType parsePrecipitationType(String code) {
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

  private static SkyStatus parseSkyStatus(String code) {
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
