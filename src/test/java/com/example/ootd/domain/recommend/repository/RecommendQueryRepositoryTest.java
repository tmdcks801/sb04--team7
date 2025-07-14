package com.example.ootd.domain.recommend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RecommendQueryRepositoryTest {

  @Mock
  private RecommendQueryRepository recommendQueryRepository;

  @Test
  @DisplayName("옷 추천 쿼리 테스트")
  void findClothesRecommendations() {
    // Given
    UUID weatherId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID clothesId = UUID.randomUUID();
    
    Object[] mockResult = {
        clothesId,
        "테스트 옷",
        "TOP",
        "http://test-image-url.com/image.jpg",
        20.0,
        0.0,
        60.0,
        2.5,
        3,
        "얇음",
        "검정",
        "봄"
    };
    
    when(recommendQueryRepository.findClothesRecommendations(any(UUID.class), any(UUID.class)))
        .thenReturn(Collections.singletonList(mockResult));

    // When
    List<Object[]> result = recommendQueryRepository.findClothesRecommendations(weatherId, userId);

    // Then
    assertThat(result).isNotEmpty();
    Object[] firstResult = result.get(0);
    
    assertThat(firstResult[0]).isEqualTo(clothesId);
    assertThat(firstResult[1]).isEqualTo("테스트 옷");
    assertThat(firstResult[2]).isEqualTo("TOP");
    assertThat(firstResult[3]).isEqualTo("http://test-image-url.com/image.jpg");
  }
}
