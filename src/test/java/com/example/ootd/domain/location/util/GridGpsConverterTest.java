package com.example.ootd.domain.location.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ootd.domain.location.util.GridGpsConverter.LatXLngY;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GridGpsConverter 테스트")
class GridGpsConverterTest {

  @Test
  @DisplayName("격자 좌표를 GPS 좌표로 변환")
  void convertGRID_GPS_GridToGps() {
    // Given
    double gridX = 98.0;
    double gridY = 76.0;

    // When
    LatXLngY result = GridGpsConverter.convertGRID_GPS(
        GridGpsConverter.MODE_GPS, gridX, gridY);

    // Then
    assertThat(result.x).isEqualTo(gridX);
    assertThat(result.y).isEqualTo(gridY);
    // 부산 남구 인근 좌표로 변환되는지 확인
    assertThat(result.lat).isBetween(35.0, 36.0);
    assertThat(result.lng).isBetween(129.0, 130.0);
  }

  @Test
  @DisplayName("경계값 테스트 - 한국 최남단")
  void convertGRID_GPS_SouthernmostKorea() {
    // Given - 제주도 마라도 인근
    double latitude = 33.1;
    double longitude = 126.3;

    // When
    LatXLngY result = GridGpsConverter.convertGRID_GPS(
        GridGpsConverter.MODE_GRID, latitude, longitude);

    // Then
    assertThat(result.x).isNotNull();
    assertThat(result.y).isNotNull();
    assertThat(result.x).isGreaterThan(0.0);
    assertThat(result.y).isGreaterThan(0.0);
  }

  @Test
  @DisplayName("경계값 테스트 - 한국 최북단")
  void convertGRID_GPS_NorthernmostKorea() {
    // Given - 함경북도 온성군 인근 (이론적 좌표)
    double latitude = 43.0;
    double longitude = 130.9;

    // When
    LatXLngY result = GridGpsConverter.convertGRID_GPS(
        GridGpsConverter.MODE_GRID, latitude, longitude);

    // Then
    assertThat(result.x).isNotNull();
    assertThat(result.y).isNotNull();
    assertThat(result.x).isGreaterThan(0.0);
    assertThat(result.y).isGreaterThan(0.0);
  }

  @Test
  @DisplayName("LatXLngY 객체 생성자 테스트")
  void latXLngY_Constructor() {
    // Given
    Double lat = 35.1595;
    Double lng = 129.0756;
    Double x = 98.0;
    Double y = 76.0;

    // When
    LatXLngY result = new LatXLngY(lat, lng, x, y);

    // Then
    assertThat(result.lat).isEqualTo(lat);
    assertThat(result.lng).isEqualTo(lng);
    assertThat(result.x).isEqualTo(x);
    assertThat(result.y).isEqualTo(y);
  }

  @Test
  @DisplayName("LatXLngY 기본 생성자 테스트")
  void latXLngY_DefaultConstructor() {
    // When
    LatXLngY result = new LatXLngY();

    // Then
    assertThat(result.lat).isNull();
    assertThat(result.lng).isNull();
    assertThat(result.x).isNull();
    assertThat(result.y).isNull();
  }
}
