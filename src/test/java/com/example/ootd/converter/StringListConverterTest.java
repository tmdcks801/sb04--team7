package com.example.ootd.converter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("StringListConverter 테스트")
class StringListConverterTest {

    private final StringListConverter converter = new StringListConverter();

    @Test
    @DisplayName("List를 String으로 변환 - 정상 케이스")
    void convertToDatabaseColumn_Success() {
        // given
        List<String> list = Arrays.asList("부산광역시", "남구", "대연동");

        // when
        String result = converter.convertToDatabaseColumn(list);

        // then
        assertThat(result).isEqualTo("부산광역시,남구,대연동");
    }

    @Test
    @DisplayName("List를 String으로 변환 - 빈 문자열 포함")
    void convertToDatabaseColumn_WithEmptyString() {
        // given
        List<String> list = Arrays.asList("부산광역시", "남구", "대연동", "");

        // when
        String result = converter.convertToDatabaseColumn(list);

        // then
        assertThat(result).isEqualTo("부산광역시,남구,대연동,");
    }

    @Test
    @DisplayName("List를 String으로 변환 - null 또는 빈 리스트")
    void convertToDatabaseColumn_NullOrEmpty() {
        // given & when & then
        assertThat(converter.convertToDatabaseColumn(null)).isEqualTo("");
        assertThat(converter.convertToDatabaseColumn(Collections.emptyList())).isEqualTo("");
    }

    @Test
    @DisplayName("String을 List로 변환 - 정상 케이스")
    void convertToEntityAttribute_Success() {
        // given
        String dbData = "부산광역시,남구,대연동";

        // when
        List<String> result = converter.convertToEntityAttribute(dbData);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("부산광역시", "남구", "대연동");
    }

    @Test
    @DisplayName("String을 List로 변환 - 빈 문자열 포함 (trailing)")
    void convertToEntityAttribute_WithTrailingEmpty() {
        // given
        String dbData = "부산광역시,남구,대연동,";

        // when
        List<String> result = converter.convertToEntityAttribute(dbData);

        // then
        assertThat(result).hasSize(4);
        assertThat(result).containsExactly("부산광역시", "남구", "대연동", "");
    }

    @Test
    @DisplayName("String을 List로 변환 - 중간에 빈 문자열 포함")
    void convertToEntityAttribute_WithMiddleEmpty() {
        // given
        String dbData = "부산광역시,,대연동";

        // when
        List<String> result = converter.convertToEntityAttribute(dbData);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("부산광역시", "", "대연동");
    }

    @Test
    @DisplayName("String을 List로 변환 - 공백 문자 제거")
    void convertToEntityAttribute_WithSpaces() {
        // given
        String dbData = " 부산광역시 , 남구 , 대연동 ";

        // when
        List<String> result = converter.convertToEntityAttribute(dbData);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("부산광역시", "남구", "대연동");
    }

    @Test
    @DisplayName("String을 List로 변환 - null 또는 빈 문자열")
    void convertToEntityAttribute_NullOrEmpty() {
        // given & when & then
        assertThat(converter.convertToEntityAttribute(null)).isEmpty();
        assertThat(converter.convertToEntityAttribute("")).isEmpty();
        assertThat(converter.convertToEntityAttribute("  ")).isEmpty();
    }

    @Test
    @DisplayName("정적 메서드 serialize 테스트")
    void serialize_Success() {
        // given
        List<String> list = Arrays.asList("부산광역시", "남구", "대연동", "");

        // when
        String result = StringListConverter.serialize(list);

        // then
        assertThat(result).isEqualTo("부산광역시,남구,대연동,");
    }

    @Test
    @DisplayName("정적 메서드 deserialize 테스트")
    void deserialize_Success() {
        // given
        String dbData = "부산광역시,남구,대연동,";

        // when
        List<String> result = StringListConverter.deserialize(dbData);

        // then
        assertThat(result).hasSize(4);
        assertThat(result).containsExactly("부산광역시", "남구", "대연동", "");
    }

    @Test
    @DisplayName("round-trip 테스트 - 변환 후 다시 변환했을 때 원본과 같아야 함")
    void roundTrip_Test() {
        // given
        List<String> original = Arrays.asList("부산광역시", "남구", "대연동", "");

        // when
        String serialized = converter.convertToDatabaseColumn(original);
        List<String> deserialized = converter.convertToEntityAttribute(serialized);

        // then
        assertThat(deserialized).isEqualTo(original);
    }
}
