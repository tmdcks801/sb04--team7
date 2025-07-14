package com.example.ootd.domain.recommend.service;

import com.example.ootd.domain.clothes.entity.ClothesType;
import com.example.ootd.domain.clothes.repository.AttributeRepository;
import com.example.ootd.domain.recommend.dto.RecommendClothesDto;
import com.example.ootd.domain.recommend.dto.ScoredClothesDto;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("옷 추천 서비스 테스트")
class ClothesSelectorTest {

    @InjectMocks
    private ClothesSelector clothesSelector;

    @Mock
    private AttributeRepository attributeRepository;

    @Test
    @DisplayName("추천 옷 선택 테스트")
    void selectRecommendedClothes() {
        // given
        ScoredClothesDto top = ScoredClothesDto.builder()
                .id(UUID.randomUUID())
                .name("Top")
                .type(ClothesType.TOP)
                .imageUrl("imageUrl")
                .thickness("얇음")
                .color("화이트")
                .season("여름")
                .temperatureCurrent(25.0)
                .windSpeed(1.0)
                .humidityCurrent(50.0)
                .precipitationAmount(0.0)
                .temperatureSensitivity(0)
                .build();

        ScoredClothesDto bottom = ScoredClothesDto.builder()
                .id(UUID.randomUUID())
                .name("Bottom")
                .type(ClothesType.BOTTOM)
                .imageUrl("imageUrl")
                .thickness("얇음")
                .color("블랙")
                .season("여름")
                .temperatureCurrent(25.0)
                .windSpeed(1.0)
                .humidityCurrent(50.0)
                .precipitationAmount(0.0)
                .temperatureSensitivity(0)
                .build();

        ScoredClothesDto shoes = ScoredClothesDto.builder()
                .id(UUID.randomUUID())
                .name("Shoes")
                .type(ClothesType.SHOES)
                .imageUrl("imageUrl")
                .thickness("보통")
                .color("화이트")
                .season("여름")
                .temperatureCurrent(25.0)
                .windSpeed(1.0)
                .humidityCurrent(50.0)
                .precipitationAmount(0.0)
                .temperatureSensitivity(0)
                .build();
        List<ScoredClothesDto> scoredClothes = List.of(top, bottom, shoes);

        // when
        List<RecommendClothesDto> result = clothesSelector.selectRecommendedClothes(scoredClothes);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.stream().map(RecommendClothesDto::type))
                .contains(ClothesType.TOP, ClothesType.BOTTOM, ClothesType.SHOES);
    }

    @Test
    @DisplayName("scoredClothes가 Null일 경우")
    void selectRecommendedClothes_Return_Null() {
        List<ScoredClothesDto> scoredClothes = List.of();

        // when
        List<RecommendClothesDto> result = clothesSelector.selectRecommendedClothes(scoredClothes);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("추운 날씨에 아우터를 추천하는지 테스트")
    void selectRecommendedClothes_shouldWearOuter() {
        // given
        ScoredClothesDto top = ScoredClothesDto.builder().type(ClothesType.TOP).temperatureCurrent(15.0).windSpeed(1.0).humidityCurrent(50.0).precipitationAmount(0.0).temperatureSensitivity(0).build();
        ScoredClothesDto bottom = ScoredClothesDto.builder().type(ClothesType.BOTTOM).temperatureCurrent(15.0).windSpeed(1.0).humidityCurrent(50.0).precipitationAmount(0.0).temperatureSensitivity(0).build();
        ScoredClothesDto shoes = ScoredClothesDto.builder().type(ClothesType.SHOES).temperatureCurrent(15.0).windSpeed(1.0).humidityCurrent(50.0).precipitationAmount(0.0).temperatureSensitivity(0).build();
        ScoredClothesDto outer = ScoredClothesDto.builder().type(ClothesType.OUTER).temperatureCurrent(15.0).windSpeed(1.0).humidityCurrent(50.0).precipitationAmount(0.0).temperatureSensitivity(0).build();
        List<ScoredClothesDto> scoredClothes = List.of(top, bottom, shoes, outer);

        // when
        List<RecommendClothesDto> result = clothesSelector.selectRecommendedClothes(scoredClothes);

        // then
        assertThat(result.stream().map(RecommendClothesDto::type)).contains(ClothesType.OUTER);
    }

    @Test
    @DisplayName("더운 날씨에 드레스를 추천하는지 테스트")
    void selectRecommendedClothes_shouldWearDress() {
        // given
        ScoredClothesDto top = ScoredClothesDto.builder().type(ClothesType.TOP).temperatureCurrent(25.0).windSpeed(1.0).humidityCurrent(50.0).precipitationAmount(0.0).temperatureSensitivity(0).build();
        ScoredClothesDto bottom = ScoredClothesDto.builder().type(ClothesType.BOTTOM).temperatureCurrent(25.0).windSpeed(1.0).humidityCurrent(50.0).precipitationAmount(0.0).temperatureSensitivity(0).build();
        ScoredClothesDto shoes = ScoredClothesDto.builder().type(ClothesType.SHOES).temperatureCurrent(25.0).windSpeed(1.0).humidityCurrent(50.0).precipitationAmount(0.0).temperatureSensitivity(0).build();
        ScoredClothesDto dress = ScoredClothesDto.builder().type(ClothesType.DRESS).temperatureCurrent(25.0).windSpeed(1.0).humidityCurrent(50.0).precipitationAmount(0.0).temperatureSensitivity(0).build();
        List<ScoredClothesDto> scoredClothes = List.of(top, bottom, shoes, dress);

        // when
        List<RecommendClothesDto> result = clothesSelector.selectRecommendedClothes(scoredClothes);

        // then
        assertThat(result.stream().map(RecommendClothesDto::type)).contains(ClothesType.DRESS);
    }


}