package com.example.ootd.domain.location.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ootd.domain.location.Location;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@ActiveProfiles("test")
@Import(com.example.ootd.config.TestConfig.class)
@DisplayName("LocationRepository 테스트")
class LocationRepositoryTest {

  @MockitoBean
  private com.example.ootd.domain.image.service.S3Service s3Service;

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private LocationRepository locationRepository;

  private Location testLocation;

  @BeforeEach
  void setUp() {
    List<String> locationNames = List.of("부산광역시", "남구", "대연동", "");
    testLocation = new Location(35.1595, 129.0756, 98, 76, locationNames);
  }

  @Test
  @DisplayName("위경도로 위치 정보 조회 성공")
  void findByLatitudeAndLongitude_Success() {
    // Given
    Location savedLocation = entityManager.persistAndFlush(testLocation);
    entityManager.clear();

    // When
    Location result = locationRepository.findByLatitudeAndLongitude(35.1595, 129.0756);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(savedLocation.getId());
    assertThat(result.getLatitude()).isEqualTo(35.1595);
    assertThat(result.getLongitude()).isEqualTo(129.0756);
    assertThat(result.getLocationX()).isEqualTo(98);
    assertThat(result.getLocationY()).isEqualTo(76);
    assertThat(result.getLocationNames()).hasSize(4);
    assertThat(result.getLocationNames().get(0)).isEqualTo("부산광역시");
  }

  @Test
  @DisplayName("존재하지 않는 위경도 조회시 null 반환")
  void findByLatitudeAndLongitude_NotFound() {
    // When
    Location result = locationRepository.findByLatitudeAndLongitude(37.5665, 126.9780);

    // Then
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("위치 정보 저장 성공")
  void save_Success() {
    // When
    Location savedLocation = locationRepository.save(testLocation);

    // Then
    assertThat(savedLocation.getId()).isNotNull();
    assertThat(savedLocation.getLatitude()).isEqualTo(35.1595);
    assertThat(savedLocation.getLongitude()).isEqualTo(129.0756);
    assertThat(savedLocation.getLocationNames()).hasSize(4);
  }

  @Test
  @DisplayName("ID로 위치 정보 조회 성공")
  void findById_Success() {
    // Given
    Location savedLocation = entityManager.persistAndFlush(testLocation);
    entityManager.clear();

    // When
    Optional<Location> result = locationRepository.findById(savedLocation.getId());

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(savedLocation.getId());
    assertThat(result.get().getLatitude()).isEqualTo(35.1595);
  }

  @Test
  @DisplayName("위치 정보 삭제 성공")
  void deleteById_Success() {
    // Given
    Location savedLocation = entityManager.persistAndFlush(testLocation);
    entityManager.clear();

    // When
    locationRepository.deleteById(savedLocation.getId());

    // Then
    Optional<Location> result = locationRepository.findById(savedLocation.getId());
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("동일한 위경도로 여러 위치 저장 가능 (중복 허용)")
  void save_DuplicateCoordinates() {
    // Given
    Location location1 = new Location(35.1595, 129.0756, 98, 76,
        List.of("부산광역시", "남구", "대연동", ""));
    Location location2 = new Location(35.1595, 129.0756, 98, 76,
        List.of("부산광역시", "남구", "대연3동", ""));

    // When
    Location saved1 = locationRepository.save(location1);
    Location saved2 = locationRepository.save(location2);

    // Then
    assertThat(saved1.getId()).isNotEqualTo(saved2.getId());
    assertThat(saved1.getLatitude()).isEqualTo(saved2.getLatitude());
    assertThat(saved1.getLongitude()).isEqualTo(saved2.getLongitude());
  }
}
