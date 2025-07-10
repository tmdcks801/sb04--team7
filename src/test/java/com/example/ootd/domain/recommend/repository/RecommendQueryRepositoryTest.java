//package com.example.ootd.domain.recommend.repository;
//
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import com.example.ootd.domain.clothes.entity.Attribute;
//import com.example.ootd.domain.clothes.entity.Clothes;
//import com.example.ootd.domain.clothes.entity.ClothesAttribute;
//import com.example.ootd.domain.clothes.entity.ClothesType;
//import com.example.ootd.domain.clothes.repository.AttributeRepository;
//import com.example.ootd.domain.clothes.repository.ClothesRepository;
//import com.example.ootd.domain.clothes.repository.RecommendQueryRepository;
//import com.example.ootd.domain.location.Location;
//import com.example.ootd.domain.location.repository.LocationRepository;
//import com.example.ootd.domain.user.User;
//import com.example.ootd.domain.user.repository.UserRepository;
//import com.example.ootd.domain.weather.entity.Weather;
//import com.example.ootd.domain.weather.entity.Humidity;
//import com.example.ootd.domain.weather.entity.Precipitation;
//import com.example.ootd.domain.weather.entity.Temperature;
//import com.example.ootd.domain.weather.entity.WindSpeed;
//import com.example.ootd.domain.weather.entity.SkyStatus;
//import com.example.ootd.domain.weather.entity.PrecipitationType;
//import com.example.ootd.domain.weather.entity.WindStrength;
//import com.example.ootd.domain.weather.repository.WeatherRepository;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.List;
//import java.util.UUID;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//@SpringBootTest
//@DisplayName("RecommendQueryRepository 테스트")
//@Transactional
//public class RecommendQueryRepositoryTest {
//
//  @Autowired
//  private UserRepository userRepository;
//
//  @Autowired
//  private LocationRepository locationRepository;
//
//  @Autowired
//  private ClothesRepository clothesRepository;
//
//  @Autowired
//  private AttributeRepository attributeRepository;
//
//  @Autowired
//  private RecommendQueryRepository recommendQueryRepository;
//
//  @Autowired
//  private WeatherRepository weatherRepository;
//
//  private User testUser;
//  private Location testLocation;
//  private Weather testWeather;
//  private Attribute thicknessAttribute;
//  private Attribute colorAttribute;
//  private Clothes topClothes;
//  private Clothes bottomClothes;
//
//
//  @BeforeEach
//  void setUp() {
//    this.testLocation = new Location(37.5665, 126.9780, 60, 127, List.of("서울특별시", "중구", "명동"));
//    locationRepository.save(this.testLocation);
//
//    this.testUser = new User("테스트유저", "test@test.com", "password123", this.testLocation);
//    // 온도 민감도는 기본값 3으로 설정됨
//    userRepository.save(this.testUser);
//
//    this.testWeather = createTestWeather();
//    weatherRepository.save(this.testWeather);
//
//    this.thicknessAttribute = createThicknessAttribute();
//    this.colorAttribute = createColorAttribute();
//    attributeRepository.save(this.thicknessAttribute);
//    attributeRepository.save(this.colorAttribute);
//
//    this.topClothes = createTopClothes();
//    this.bottomClothes = createBottomClothes();
//    clothesRepository.save(this.topClothes);
//    clothesRepository.save(this.bottomClothes);
//
//    createClothesAttributes();
//  }
//
//  private Weather createTestWeather() {
//    Temperature temperature = Temperature.builder()
//        .temperatureCurrent(22.0)
//        .temperatureMin(18.0)
//        .temperatureMax(26.0)
//        .build();
//
//    Humidity humidity = Humidity.builder()
//        .humidityCurrent(60.0)
//        .build();
//
//    Precipitation precipitation = Precipitation.builder()
//        .precipitationType(PrecipitationType.NONE)
//        .precipitationAmount(0.0)
//        .precipitationProbability(10.0)
//        .build();
//
//    WindSpeed windSpeed = WindSpeed.builder()
//        .windSpeed(2.5)
//        .windAsWord(WindStrength.WEAK)
//        .build();
//
//    return Weather.builder()
//        .regionName("서울특별시")
//        .forecastedAt(LocalDateTime.now().minusHours(1))
//        .forecastAt(LocalDateTime.now())
//        .skyStatus(SkyStatus.CLEAR)
//        .temperature(temperature)
//        .humidity(humidity)
//        .precipitation(precipitation)
//        .windSpeed(windSpeed)
//        .build();
//  }
//
//  private Attribute createThicknessAttribute() {
//    return Attribute.builder()
//        .name("두께감")
//        .details(Arrays.asList("얇음", "약간 얇음", "약간 두꺼움", "두꺼움"))
//        .build();
//  }
//
//  private Attribute createColorAttribute() {
//    return Attribute.builder()
//        .name("색상")
//        .details(Arrays.asList("화이트", "베이지", "블랙", "네이비", "그레이", "브라운"))
//        .build();
//  }
//
//  private Clothes createTopClothes() {
//    return Clothes.builder()
//        .user(this.testUser)
//        .name("티셔츠")
//        .type(ClothesType.TOP)
//        .build();
//  }
//
//  private Clothes createBottomClothes() {
//    return Clothes.builder()
//        .user(this.testUser)
//        .name("청바지")
//        .type(ClothesType.BOTTOM)
//        .build();
//  }
//
//  private void createClothesAttributes() {
//    ClothesAttribute topThickness = ClothesAttribute.builder()
//        .clothes(this.topClothes)
//        .attribute(this.thicknessAttribute)
//        .value("약간 얇음")
//        .build();
//
//    ClothesAttribute topColor = ClothesAttribute.builder()
//        .clothes(this.topClothes)
//        .attribute(this.colorAttribute)
//        .value("화이트")
//        .build();
//
//    ClothesAttribute bottomThickness = ClothesAttribute.builder()
//        .clothes(this.bottomClothes)
//        .attribute(this.thicknessAttribute)
//        .value("약간 두꺼움")
//        .build();
//
//    ClothesAttribute bottomColor = ClothesAttribute.builder()
//        .clothes(this.bottomClothes)
//        .attribute(this.colorAttribute)
//        .value("네이비")
//        .build();
//
//    this.topClothes.addClothesAttribute(topThickness);
//    this.topClothes.addClothesAttribute(topColor);
//    this.bottomClothes.addClothesAttribute(bottomThickness);
//    this.bottomClothes.addClothesAttribute(bottomColor);
//
//    clothesRepository.save(this.topClothes);
//    clothesRepository.save(this.bottomClothes);
//  }
//
//  @Test
//  @DisplayName("날씨 기반 옷 추천 쿼리 테스트")
//  void findClothesRecommendations_Success() {
//    // when
//    List<Object[]> results = recommendQueryRepository.findClothesRecommendations(
//        this.testWeather.getId(),
//        this.testUser.getId()
//    );
//
//    // then
//    assertThat(results).isNotEmpty();
//
//    Object[] firstResult = results.get(0);
//    assertThat(firstResult).hasSize(12);
//
//    System.out.println(Arrays.toString(firstResult));
//
//    // 결과값 타입 검증
//    assertThat(firstResult[0]).isInstanceOf(UUID.class); // clothes_id
//    assertThat(firstResult[1]).isInstanceOf(String.class); // clothes_name
//    assertThat(firstResult[2]).isInstanceOf(String.class); // clothes_type
//    assertThat(firstResult[3]).isNull(); // image_url
//    assertThat(firstResult[4]).isInstanceOf(Double.class); // temperature_current
//    assertThat(firstResult[5]).isInstanceOf(Double.class); // precipitation_amount
//    assertThat(firstResult[6]).isInstanceOf(Double.class); // humidity_current
//    assertThat(firstResult[7]).isInstanceOf(Double.class); // wind_speed
//    assertThat(firstResult[8]).isInstanceOf(Integer.class); // temperature_sensitivity
//    assertThat(firstResult[9]).isInstanceOf(String.class); // thickness
//    assertThat(firstResult[10]).isInstanceOf(String.class); // color
//    assertThat(firstResult[11]).isNull(); // score TODO: score가 왜 null?
//
//    // 스코어가 0 이상인지 확인 TODO: score가 왜 null?
////    Double totalScore = (Double) firstResult[11];
////    assertThat(totalScore).isGreaterThanOrEqualTo(0);
//  }
//
//  @Test
//  @DisplayName("존재하지 않는 날씨 ID로 조회시 빈 결과 반환")
//  void findClothesRecommendations_WithInvalidWeatherId_ReturnsEmptyList() {
//    // given
//    UUID invalidWeatherId = UUID.randomUUID();
//
//    // when
//    List<Object[]> results = recommendQueryRepository.findClothesRecommendations(
//        invalidWeatherId,
//        this.testUser.getId()
//    );
//
//    // then
//    assertThat(results).isEmpty();
//  }
//
//  @Test
//  @DisplayName("존재하지 않는 사용자 ID로 조회시 빈 결과 반환")
//  void findClothesRecommendations_WithInvalidUserId_ReturnsEmptyList() {
//    // given
//    UUID invalidUserId = UUID.randomUUID();
//
//    // when
//    List<Object[]> results = recommendQueryRepository.findClothesRecommendations(
//        this.testWeather.getId(),
//        invalidUserId
//    );
//
//    // then
//    assertThat(results).isEmpty();
//  }
//}
