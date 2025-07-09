package com.example.ootd.domain.weather.service;

import com.example.ootd.domain.location.dto.WeatherAPILocation;
import com.example.ootd.domain.location.mapper.WeatherLocationMapper;
import com.example.ootd.domain.location.service.LocationService;
import com.example.ootd.domain.weather.dto.WeatherDto;
import com.example.ootd.domain.weather.dto.WeatherSummaryDto;
import com.example.ootd.domain.weather.entity.Precipitation;
import com.example.ootd.domain.weather.entity.Weather;
import com.example.ootd.domain.weather.mapper.WeatherMapper;
import com.example.ootd.domain.weather.repository.WeatherRepository;
import com.example.ootd.exception.weather.InvalidCoordinatesException;
import com.example.ootd.exception.weather.WeatherDataInsufficientException;
import com.example.ootd.exception.weather.WeatherNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class WeatherServiceImpl implements WeatherService {

  private final LocationService locationService;
  private final WeatherRepository weatherRepository;

  @Override
  public WeatherSummaryDto getSummaryWeather(double latitude, double longitude) {
    return null;
  }

  @Override
  public List<WeatherDto> getThreeDayWeather(double latitude, double longitude,
      LocalDateTime targetTime) {
    try {
      // 좌표 입력 로깅
      log.debug("3일간 날씨 요청 - latitude: {}, longitude: {}, targetTime: {}",
          latitude, longitude, targetTime);

      // 좌표 유효성 검사
      if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
        throw new InvalidCoordinatesException(latitude, longitude);
      }

      // 1. 위경도로 지역명 조회
      WeatherAPILocation location = locationService.getGridAndLocation(latitude, longitude);
      List<String> locationNames = location.locationNames();

      if (locationNames == null || locationNames.size() < 2) {
        WeatherNotFoundException exception = new WeatherNotFoundException();
        exception.addDetail("receivedLocationNames", String.valueOf(locationNames));
        throw exception;
      }

      // 2. 지역명 생성: 시도 + 군구 (예: 부산 남구)
      String regionName = locationNames.get(0) + " " + locationNames.get(1);
      log.debug("사용할 지역명: {}", regionName);

      // 3. 3일간 날짜 범위 설정
      LocalDateTime startDate = targetTime.toLocalDate().atStartOfDay();
      LocalDateTime endDate = targetTime.toLocalDate().plusDays(2).atTime(LocalTime.MAX);
      log.debug("날짜 범위: {} ~ {}", startDate, endDate);

      // 4. 해당 기간의 날씨 데이터 조회
      List<Weather> allWeatherData = weatherRepository.findWeathersByRegionAndDateRange(
          regionName, startDate, endDate);

      log.debug("조회된 날씨 데이터 개수: {}", allWeatherData.size());
      if (!allWeatherData.isEmpty()) {
        log.debug("조회된 날씨 데이터 범위: {} ~ {}",
            allWeatherData.get(0).getForecastAt(),
            allWeatherData.get(allWeatherData.size() - 1).getForecastAt());
      }

      if (allWeatherData.isEmpty()) {
        WeatherNotFoundException exception = new WeatherNotFoundException(regionName,
            startDate + " ~ " + endDate);
        throw exception;
      }

      // 4. 오늘/내일/모래 날씨 추출
      List<WeatherDto> result = new ArrayList<>();

      for (int dayOffset = 0; dayOffset < 3; dayOffset++) {
        LocalDate targetDate = targetTime.toLocalDate().plusDays(dayOffset);
        LocalDateTime targetDateTime = targetDate.atTime(targetTime.toLocalTime());

        log.debug("{}일 후 날씨 추출 시도: {}", dayOffset, targetDate);

        // 오늘은 정확한 데이터, 내일/모래는 최악값 사용
        boolean isToday = (dayOffset == 0);
        WeatherDto dayWeather = findBestWeatherForDay(
            allWeatherData, targetDateTime, location, isToday);

        if (dayWeather != null) {
          result.add(dayWeather);
        } else {
          log.warn("날짜 {}({}일후)의 날씨 데이터를 찾을 수 없습니다.", targetDate, dayOffset);
        }
      }

      log.debug("최종 반환할 날씨 데이터 개수: {}", result.size());

      // 데이터 충분성 검사
      if (result.size() < 3) {
        throw new WeatherDataInsufficientException(3, result.size());
      }

      return result;

    } catch (InvalidCoordinatesException | WeatherNotFoundException |
             WeatherDataInsufficientException e) {
      // 예상된 비즈니스 예외는 그대로 전파
      throw e;
    } catch (Exception e) {
      log.error("3일간 날씨 조회 실패 - latitude: {}, longitude: {}, targetTime: {}",
          latitude, longitude, targetTime, e);
      throw new WeatherNotFoundException();
    }
  }

  // 특정 날짜에 가장 적합한 날씨 데이터 찾기
  private WeatherDto findBestWeatherForDay(List<Weather> allWeatherData,
      LocalDateTime targetDateTime,
      WeatherAPILocation location,
      boolean isToday) {
    LocalDate targetDate = targetDateTime.toLocalDate();

    // 해당 날짜의 모든 날씨 데이터 필터링
    List<Weather> dayWeatherList = allWeatherData.stream()
        .filter(w -> w.getForecastAt().toLocalDate().equals(targetDate))
        .toList();

    if (dayWeatherList.isEmpty()) {
      return null;
    }

    // 1. 정확한 시간 매칭 우선 찾기
    Optional<Weather> exactMatch = dayWeatherList.stream()
        .filter(w -> w.getForecastAt().equals(targetDateTime))
        .findFirst();

    Weather baseWeather;
    if (exactMatch.isPresent()) {
      baseWeather = exactMatch.get();
    } else {
      // 2. 가장 가까운 시간의 날씨 데이터 사용
      baseWeather = dayWeatherList.stream()
          .min(Comparator.comparing(w ->
              Math.abs(w.getForecastAt().toLocalTime().toSecondOfDay() -
                  targetDateTime.toLocalTime().toSecondOfDay())))
          .orElse(dayWeatherList.get(0));
    }

    // 3. 강수 데이터 처리 전략
    if (isToday) {
      // 오늘: 정확한 시간의 데이터 그대로 사용
      return WeatherMapper.toDto(baseWeather, WeatherLocationMapper.toEntity(location));
    } else {
      // 내일/모래: 강수 관련 데이터는 가장 안좋은 것으로 대체
      Precipitation worstPrecipitation = findWorstPrecipitation(dayWeatherList);
      Weather enhancedWeather = enhanceWeatherWithWorstPrecipitation(baseWeather,
          worstPrecipitation);
      return WeatherMapper.toDto(enhancedWeather, WeatherLocationMapper.toEntity(location));
    }
  }

  // 하루 중 가장 안좋은 강수 데이터 찾기
  private Precipitation findWorstPrecipitation(List<Weather> dayWeatherList) {
    return dayWeatherList.stream()
        .map(Weather::getPrecipitation)
        .max(Comparator
            .comparing(Precipitation::getPrecipitationProbability) // 1순위: 강수확률
            .thenComparing(Precipitation::getPrecipitationAmount)   // 2순위: 강수량
            .thenComparing(p -> p.getPrecipitationType().ordinal()) // 3순위: 강수형태
        )
        .orElse(dayWeatherList.get(0).getPrecipitation());
  }

  // 기본 날씨에 최악 강수 데이터를 적용한 새로운 Weather 객체 생성
  private Weather enhanceWeatherWithWorstPrecipitation(Weather baseWeather,
      Precipitation worstPrecipitation) {
    // 기본 Weather 데이터를 복사하고 강수 데이터만 교체
    return Weather.builder()
        .id(baseWeather.getId())
        .regionName(baseWeather.getRegionName())
        .forecastedAt(baseWeather.getForecastedAt())
        .forecastAt(baseWeather.getForecastAt())
        .temperature(baseWeather.getTemperature())
        .humidity(baseWeather.getHumidity())
        .precipitation(worstPrecipitation) // 최악 강수 데이터 사용
        .windSpeed(baseWeather.getWindSpeed())
        .skyStatus(baseWeather.getSkyStatus())
        .build();
  }
}
