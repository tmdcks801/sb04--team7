package com.example.ootd.domain.location.mapper;

import com.example.ootd.domain.location.Location;
import com.example.ootd.domain.location.dto.WeatherAPILocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("WeatherLocationMapper 테스트")
class WeatherLocationMapperTest {

    @Test
    @DisplayName("WeatherAPILocation을 Location 엔티티로 매핑 성공")
    void toEntity_Success() {
        // Given
        WeatherAPILocation dto = new WeatherAPILocation(
            35.1595, 129.0756, 98, 76,
            List.of("부산광역시", "남구", "대연동", "")
        );

        // When
        Location result = WeatherLocationMapper.toEntity(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLatitude()).isEqualTo(35.1595);
        assertThat(result.getLongitude()).isEqualTo(129.0756);
        assertThat(result.getLocationX()).isEqualTo(98);
        assertThat(result.getLocationY()).isEqualTo(76);
        assertThat(result.getLocationNames()).hasSize(4);
        assertThat(result.getLocationNames().get(0)).isEqualTo("부산광역시");
        assertThat(result.getLocationNames().get(1)).isEqualTo("남구");
        assertThat(result.getLocationNames().get(2)).isEqualTo("대연동");
        assertThat(result.getLocationNames().get(3)).isEqualTo("");
    }

    @Test
    @DisplayName("서울 강남구 매핑 테스트")
    void toEntity_SeoulGangnam() {
        // Given
        WeatherAPILocation dto = new WeatherAPILocation(
            37.5172, 127.0473, 61, 125,
            List.of("서울특별시", "강남구", "역삼동", "")
        );

        // When
        Location result = WeatherLocationMapper.toEntity(dto);

        // Then
        assertThat(result.getLatitude()).isEqualTo(37.5172);
        assertThat(result.getLongitude()).isEqualTo(127.0473);
        assertThat(result.getLocationX()).isEqualTo(61);
        assertThat(result.getLocationY()).isEqualTo(125);
        assertThat(result.getLocationNames().get(0)).isEqualTo("서울특별시");
        assertThat(result.getLocationNames().get(1)).isEqualTo("강남구");
    }

    @Test
    @DisplayName("제주도 매핑 테스트")
    void toEntity_Jeju() {
        // Given
        WeatherAPILocation dto = new WeatherAPILocation(
            33.4996, 126.5312, 52, 38,
            List.of("제주특별자치도", "제주시", "일도일동", "")
        );

        // When
        Location result = WeatherLocationMapper.toEntity(dto);

        // Then
        assertThat(result.getLatitude()).isEqualTo(33.4996);
        assertThat(result.getLongitude()).isEqualTo(126.5312);
        assertThat(result.getLocationX()).isEqualTo(52);
        assertThat(result.getLocationY()).isEqualTo(38);
        assertThat(result.getLocationNames().get(0)).isEqualTo("제주특별자치도");
        assertThat(result.getLocationNames().get(1)).isEqualTo("제주시");
    }

    @Test
    @DisplayName("지역명이 부족한 경우 매핑 테스트")
    void toEntity_InsufficientLocationNames() {
        // Given
        WeatherAPILocation dto = new WeatherAPILocation(
            35.1595, 129.0756, 98, 76,
            List.of("부산광역시") // 부족한 지역명
        );

        // When
        Location result = WeatherLocationMapper.toEntity(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLocationNames()).hasSize(1);
        assertThat(result.getLocationNames().get(0)).isEqualTo("부산광역시");
    }

    @Test
    @DisplayName("빈 지역명 리스트 매핑 테스트")
    void toEntity_EmptyLocationNames() {
        // Given
        WeatherAPILocation dto = new WeatherAPILocation(
            35.1595, 129.0756, 98, 76,
            List.of()
        );

        // When
        Location result = WeatherLocationMapper.toEntity(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLocationNames()).isEmpty();
        assertThat(result.getLatitude()).isEqualTo(35.1595);
        assertThat(result.getLongitude()).isEqualTo(129.0756);
    }

    @Test
    @DisplayName("극한 좌표값 매핑 테스트")
    void toEntity_ExtremeCoordinates() {
        // Given
        WeatherAPILocation dto = new WeatherAPILocation(
            90.0, 180.0, 999, 999,
            List.of("극지방", "테스트구역", "", "")
        );

        // When
        Location result = WeatherLocationMapper.toEntity(dto);

        // Then
        assertThat(result.getLatitude()).isEqualTo(90.0);
        assertThat(result.getLongitude()).isEqualTo(180.0);
        assertThat(result.getLocationX()).isEqualTo(999);
        assertThat(result.getLocationY()).isEqualTo(999);
    }
}
