package com.example.ootd.domain.weather.mapper;

import com.example.ootd.config.TestMailConfig;
import com.example.ootd.domain.location.Location;
import com.example.ootd.domain.weather.dto.WeatherDto;
import com.example.ootd.domain.weather.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
@Import(TestMailConfig.class)
@DisplayName("WeatherMapper 테스트")
class WeatherMapperTest {

    private Weather testWeather;
    private Location testLocation;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2024, 12, 15, 14, 0);

        // 테스트 위치 생성
        List<String> locationNames = List.of("부산광역시", "남구", "대연동", "");
        testLocation = new Location(35.1595, 129.0756, 98, 76, locationNames);
        ReflectionTestUtils.setField(testLocation, "id", UUID.randomUUID());

        // 테스트 날씨 생성
        testWeather = Weather.builder()
            .regionName("부산광역시 남구")
            .forecastedAt(testDateTime.minusHours(2))
            .forecastAt(testDateTime)
            .skyStatus(SkyStatus.CLEAR)
            .precipitation(Precipitation.builder()
                .precipitationType(PrecipitationType.NONE)
                .precipitationAmount(0.0)
                .precipitationProbability(20.0)
                .build())
            .temperature(Temperature.builder()
                .temperatureCurrent(15.0)
                .temperatureMin(10.0)
                .temperatureMax(20.0)
                .temperatureComparedToDayBefore(-1.0)
                .build())
            .humidity(Humidity.builder()
                .humidityCurrent(60.0)
                .humidityComparedToDayBefore(5.0)
                .build())
            .windSpeed(WindSpeed.builder()
                .windSpeed(3.5)
                .windAsWord(WindStrength.WEAK)
                .build())
            .build();

        ReflectionTestUtils.setField(testWeather, "id", UUID.randomUUID());
    }

    @Test
    @DisplayName("Weather와 Location을 WeatherDto로 매핑 성공")
    void toDto_Success() {
        // When
        WeatherDto result = WeatherMapper.toDto(testWeather, testLocation);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(testWeather.getId());
        assertThat(result.forecastedAt()).isEqualTo(testWeather.getForecastedAt());
        assertThat(result.forecastAt()).isEqualTo(testWeather.getForecastAt());
        assertThat(result.locationNames()).isEqualTo(testLocation.getLocationNames());
        assertThat(result.skyStatus()).isEqualTo(SkyStatus.CLEAR);

        // Temperature 검증
        assertThat(result.temperature().current()).isEqualTo(15.0);
        assertThat(result.temperature().min()).isEqualTo(10.0);
        assertThat(result.temperature().max()).isEqualTo(20.0);
        assertThat(result.temperature().comparedToDayBefore()).isEqualTo(-1.0);

        // Precipitation 검증
        assertThat(result.precipitation().type()).isEqualTo(PrecipitationType.NONE);
        assertThat(result.precipitation().amount()).isEqualTo(0.0);
        assertThat(result.precipitation().probability()).isEqualTo(0.2);

        // Humidity 검증
        assertThat(result.humidity().current()).isEqualTo(60.0);
        assertThat(result.humidity().comparedToDayBefore()).isEqualTo(5.0);

        // WindSpeed 검증
        assertThat(result.windSpeed().speed()).isEqualTo(3.5);
        assertThat(result.windSpeed().asWord()).isEqualTo(WindStrength.WEAK);
    }

    @Test
    @DisplayName("비 오는 날씨 매핑 테스트")
    void toDto_RainyWeather() {
        // Given
        Weather rainyWeather = Weather.builder()
            .regionName("부산광역시 남구")
            .forecastedAt(testDateTime.minusHours(2))
            .forecastAt(testDateTime)
            .skyStatus(SkyStatus.CLOUDY)
            .precipitation(Precipitation.builder()
                .precipitationType(PrecipitationType.RAIN)
                .precipitationAmount(5.0)
                .precipitationProbability(80.0)
                .build())
            .temperature(Temperature.builder()
                .temperatureCurrent(12.0)
                .temperatureMin(8.0)
                .temperatureMax(16.0)
                .temperatureComparedToDayBefore(-3.0)
                .build())
            .humidity(Humidity.builder()
                .humidityCurrent(85.0)
                .humidityComparedToDayBefore(15.0)
                .build())
            .windSpeed(WindSpeed.builder()
                .windSpeed(7.2)
                .windAsWord(WindStrength.STRONG)
                .build())
            .build();

        ReflectionTestUtils.setField(rainyWeather, "id", UUID.randomUUID());

        // When
        WeatherDto result = WeatherMapper.toDto(rainyWeather, testLocation);

        // Then
        assertThat(result.skyStatus()).isEqualTo(SkyStatus.CLOUDY);
        assertThat(result.precipitation().type()).isEqualTo(PrecipitationType.RAIN);
        assertThat(result.precipitation().amount()).isEqualTo(5.0);
        assertThat(result.precipitation().probability()).isEqualTo(0.8);
        assertThat(result.windSpeed().asWord()).isEqualTo(WindStrength.STRONG);
    }

    @Test
    @DisplayName("눈 오는 날씨 매핑 테스트")
    void toDto_SnowyWeather() {
        // Given
        Weather snowyWeather = Weather.builder()
            .regionName("부산광역시 남구")
            .forecastedAt(testDateTime.minusHours(2))
            .forecastAt(testDateTime)
            .skyStatus(SkyStatus.CLOUDY)
            .precipitation(Precipitation.builder()
                .precipitationType(PrecipitationType.SNOW)
                .precipitationAmount(2.0)
                .precipitationProbability(70.0)
                .build())
            .temperature(Temperature.builder()
                .temperatureCurrent(-2.0)
                .temperatureMin(-5.0)
                .temperatureMax(1.0)
                .temperatureComparedToDayBefore(-8.0)
                .build())
            .humidity(Humidity.builder()
                .humidityCurrent(90.0)
                .humidityComparedToDayBefore(20.0)
                .build())
            .windSpeed(WindSpeed.builder()
                .windSpeed(5.8)
                .windAsWord(WindStrength.MODERATE)
                .build())
            .build();

        ReflectionTestUtils.setField(snowyWeather, "id", UUID.randomUUID());

        // When
        WeatherDto result = WeatherMapper.toDto(snowyWeather, testLocation);

        // Then
        assertThat(result.precipitation().type()).isEqualTo(PrecipitationType.SNOW);
        assertThat(result.temperature().current()).isEqualTo(-2.0);
        assertThat(result.windSpeed().asWord()).isEqualTo(WindStrength.MODERATE);
    }

    @Test
    @DisplayName("모든 필드가 null이 아닌지 검증")
    void toDto_AllFieldsNotNull() {
        // When
        WeatherDto result = WeatherMapper.toDto(testWeather, testLocation);

        // Then
        assertThat(result.id()).isNotNull();
        assertThat(result.forecastedAt()).isNotNull();
        assertThat(result.forecastAt()).isNotNull();
        assertThat(result.locationNames()).isNotNull();
        assertThat(result.skyStatus()).isNotNull();
        assertThat(result.temperature()).isNotNull();
        assertThat(result.precipitation()).isNotNull();
        assertThat(result.humidity()).isNotNull();
        assertThat(result.windSpeed()).isNotNull();
    }
}
