package com.example.ootd;

import com.example.ootd.domain.clothes.entity.Attribute;
import com.example.ootd.domain.image.entity.Image;
import com.example.ootd.domain.user.Gender;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.UserRole;
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
import java.util.List;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

public class TestEntityFactory {

  public static User createUser() {
    User user = new User(
        "test-name",
        "test@gmail.com",
        "test-password",
        UserRole.ROLE_USER,
        false,
        null,
        null,
        Gender.MALE,
        LocalDate.of(1999, 4, 13),
        3,
        false,
        null
    );

    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

    return user;
  }

  public static User createUser(String email, String name) {
    User user = new User(
        name,
        email,
        "test-password",
        UserRole.ROLE_USER,
        false,
        null,
        null,
        Gender.MALE,
        LocalDate.of(1999, 4, 13),
        3,
        false,
        null
    );

    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.now());

    return user;
  }

  // 테스트용 유저 객체 반환
  public static User createUserWithoutId(String uniqueSuffix) {

    return new User(
        "test-name-no-id" + uniqueSuffix,
        "testNoId" + uniqueSuffix + "@gmail.com",

        "test-password",
        UserRole.ROLE_USER,
        false,
        null,
        null,
        Gender.OTHER,
        LocalDate.of(2000, 11, 11),
        3,
        false,
        null
    );
  }

  // 테스트용 이미지 객체 반환
  public static Image createImageWithoutId(String uniqueSuffix) {

    return new Image("https://test-bucket.s3.region.amazonaws.com/test" + uniqueSuffix + ".jpg",
        "test" + uniqueSuffix + ".jpg");
  }

  public static Image createImage() {

    Image image = new Image("https://test-bucket.s3.region.amazonaws.com/test.jpg",
        "test.jpg");

    ReflectionTestUtils.setField(image, "id", UUID.randomUUID());

    return image;
  }

  // 테스트용 의상 속성 정의 객체 반환
  public static Attribute createAttribute() {

    Attribute attribute = new Attribute(
        "test", List.of("test1", "test2", "test3")
    );

    ReflectionTestUtils.setField(attribute, "id", UUID.randomUUID());

    return attribute;
  }

  // 테스트용 날씨 객체 반환
  public static Weather createWeatherWithoutId(SkyStatus skyStatus,
      PrecipitationType precipitationType) {

    return Weather.builder()
        .regionName("서울")
        .forecastedAt(LocalDateTime.now())
        .forecastAt(LocalDateTime.now())
        .skyStatus(skyStatus)
        .precipitation(
            Precipitation.builder().precipitationType(precipitationType)
                .precipitationAmount(0d).precipitationProbability(0d)
                .build()
        )
        .temperature(
            Temperature.builder().temperatureCurrent(0d).temperatureMin(0d).temperatureMax(0d)
                .temperatureComparedToDayBefore(0d).build())
        .humidity(Humidity.builder().humidityCurrent(0d).humidityComparedToDayBefore(0d).build())
        .windSpeed(WindSpeed.builder().windSpeed(0d).windAsWord(WindStrength.WEAK).build())
        .build();
  }
}
