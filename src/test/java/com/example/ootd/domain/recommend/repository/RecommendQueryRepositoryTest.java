package com.example.ootd.domain.recommend.repository;

import com.example.ootd.domain.clothes.entity.Clothes;
import com.example.ootd.domain.clothes.entity.ClothesType;
import com.example.ootd.domain.clothes.repository.ClothesRepository;
import com.example.ootd.domain.image.entity.Image;
import com.example.ootd.domain.image.repository.ImageRepository;
import com.example.ootd.domain.location.Location;
import com.example.ootd.domain.location.repository.LocationRepository;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.domain.weather.entity.Humidity;
import com.example.ootd.domain.weather.entity.Precipitation;
import com.example.ootd.domain.weather.entity.PrecipitationType;
import com.example.ootd.domain.weather.entity.SkyStatus;
import com.example.ootd.domain.weather.entity.Temperature;
import com.example.ootd.domain.weather.entity.Weather;
import com.example.ootd.domain.weather.entity.WindSpeed;
import com.example.ootd.domain.weather.entity.WindStrength;
import com.example.ootd.domain.weather.repository.WeatherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("RecommendQueryRepository 테스트")
@Transactional
class RecommendQueryRepositoryTest {

    @Autowired
    private RecommendQueryRepository recommendQueryRepository;

    @Autowired
    private ClothesRepository clothesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WeatherRepository weatherRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private LocationRepository locationRepository;

    private User testUser;
    private Weather testWeather;
    private Clothes testClothes;
    private Image testImage;
    private Location testLocation;

    @BeforeEach
    void setUp() {
        setupLocation();
        setupUser();
        setupWeather();
        setupImage();
        setupClothes();
    }

    private void setupLocation() {
        testLocation = new Location(37.5665, 126.9780, 60, 127, 
            Arrays.asList("서울특별시", "중구", "명동"));
        locationRepository.save(testLocation);
    }

    private void setupUser() {
        testUser = new User("테스트유저", "test@test.com", "password123", testLocation);
        userRepository.save(testUser);
    }

    private void setupWeather() {
        Temperature temperature = Temperature.builder()
            .temperatureCurrent(20.0)
            .temperatureMin(15.0)
            .temperatureMax(25.0)
            .temperatureComparedToDayBefore(2.0)
            .build();
        
        Precipitation precipitation = Precipitation.builder()
            .precipitationType(PrecipitationType.RAIN)
            .precipitationAmount(5.0)
            .precipitationProbability(30.0)
            .build();
        
        Humidity humidity = Humidity.builder()
            .humidityCurrent(60.0)
            .humidityComparedToDayBefore(5.0)
            .build();
        
        WindSpeed windSpeed = WindSpeed.builder()
            .windSpeed(10.0)
            .windAsWord(WindStrength.MODERATE)
            .build();

        testWeather = Weather.builder()
            .regionName("서울특별시 중구")
            .forecastedAt(LocalDateTime.now())
            .forecastAt(LocalDateTime.now().plusHours(1))
            .skyStatus(SkyStatus.CLEAR)
            .precipitation(precipitation)
            .temperature(temperature)
            .humidity(humidity)
            .windSpeed(windSpeed)
            .build();
        
        weatherRepository.save(testWeather);
    }

    private void setupImage() {
        testImage = new Image("https://example.com/image.jpg", "test-image.jpg");
        imageRepository.save(testImage);
    }

    private void setupClothes() {
        testClothes = new Clothes(testUser, testImage, "테스트 상의", ClothesType.TOP);
        clothesRepository.save(testClothes);
    }

    @Test
    @DisplayName("의상 추천 조회 - 정상 케이스")
    void findClothesRecommendations_Success() {
        // when
        List<Object[]> results = recommendQueryRepository.findClothesRecommendations(
            testWeather.getId(), testUser.getId());

        // then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);

        Object[] result = results.get(0);
        
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("의상 추천 조회 - 해당 사용자의 의상이 없는 경우")
    void findClothesRecommendations_EmptyResult() {
        // given
        User anotherUser = new User("다른유저", "another@test.com", "password123", testLocation);
        userRepository.save(anotherUser);

        // when
        List<Object[]> results = recommendQueryRepository.findClothesRecommendations(
            testWeather.getId(), anotherUser.getId());

        // then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("의상 추천 조회 - 존재하지 않는 날씨 ID")
    void findClothesRecommendations_NonExistentWeatherId() {
        // given
        UUID nonExistentWeatherId = UUID.randomUUID();

        // when
        List<Object[]> results = recommendQueryRepository.findClothesRecommendations(
            nonExistentWeatherId, testUser.getId());

        // then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("의상 추천 조회 - 존재하지 않는 사용자 ID")
    void findClothesRecommendations_NonExistentUserId() {
        // given
        UUID nonExistentUserId = UUID.randomUUID();

        // when
        List<Object[]> results = recommendQueryRepository.findClothesRecommendations(
            testWeather.getId(), nonExistentUserId);

        // then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("의상 추천 조회 - 상하의")
    void findClothesRecommendations_MultipleClothesTypes() {
        // given
        Clothes bottomClothes = new Clothes(testUser, null, "테스트 하의", ClothesType.BOTTOM);

        clothesRepository.save(bottomClothes);

        // when
        List<Object[]> results = recommendQueryRepository.findClothesRecommendations(
            testWeather.getId(), testUser.getId());

        // then
        assertThat(results).hasSize(2);
    }
}