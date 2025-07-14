package com.example.ootd.domain.recommend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.ootd.domain.clothes.entity.ClothesType;
import com.example.ootd.domain.recommend.dto.RecommendClothesDto;
import com.example.ootd.domain.recommend.dto.RecommendationDto;
import com.example.ootd.domain.recommend.dto.ScoredClothesDto;
import com.example.ootd.domain.recommend.repository.RecommendQueryRepository;
import com.example.ootd.domain.recommend.service.impl.RecommendServiceImpl;
import com.example.ootd.domain.user.User;
import com.example.ootd.security.CustomUserDetails;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class RecommendServiceImplTest {

  @Mock
  private RecommendQueryRepository recommendQueryRepository;
  
  @Mock
  private ClothesCalculator scoreCalculator;
  
  @Mock
  private ClothesSelector clothesSelector;
  
  @Mock
  private CacheManager cacheManager;
  
  @Mock
  private SecurityContext securityContext;
  
  @Mock
  private Authentication authentication;
  
  @Mock
  private CustomUserDetails userDetails;
  
  @Mock
  private User user;
  
  @Mock
  private Cache cache;

  @InjectMocks
  private RecommendServiceImpl recommendService;

  private UUID testUserId;
  private UUID testWeatherId;
  private UUID testClothesId;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    testWeatherId = UUID.randomUUID();
    testClothesId = UUID.randomUUID();
  }

  @Test
  @DisplayName("옷 추천 성공 테스트")
  void recommend_Success() {
    // Given
    Object[] mockQueryResult = {
        testClothesId.toString(),
        "테스트 옷",
        "TOP",
        "http://test-image.jpg",
        20.0,
        0.0,
        60.0,
        2.5,
        3,
        "얇음",
        "검정",
        "봄"
    };
    
    List<Object[]> queryResults = Collections.singletonList(mockQueryResult);
    
    RecommendClothesDto recommendClothes = RecommendClothesDto.builder()
        .clothesId(testClothesId)
        .name("테스트 옷")
        .type(ClothesType.TOP)
        .imageUrl("http://test-image.jpg")
        .attributes(Collections.emptyList())
        .build();

    // Security Context 설정
    try (MockedStatic<SecurityContextHolder> securityContextHolderMock = 
         Mockito.mockStatic(SecurityContextHolder.class)) {
      
      securityContextHolderMock.when(SecurityContextHolder::getContext)
          .thenReturn(securityContext);
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(userDetails);
      when(userDetails.getUser()).thenReturn(user);
      when(user.getId()).thenReturn(testUserId);

      when(recommendQueryRepository.findClothesRecommendations(testWeatherId, testUserId))
          .thenReturn(queryResults);
      when(scoreCalculator.calculateScore(any(ScoredClothesDto.class)))
          .thenReturn(85.0);
      when(clothesSelector.selectRecommendedClothes(anyList()))
          .thenReturn(Collections.singletonList(recommendClothes));

      // When
      RecommendationDto result = recommendService.recommend(testWeatherId);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.weatherId()).isEqualTo(testWeatherId);
      assertThat(result.userId()).isEqualTo(testUserId);
      assertThat(result.clothes()).hasSize(1);
      assertThat(result.clothes().get(0).clothesId()).isEqualTo(testClothesId);
      assertThat(result.clothes().get(0).name()).isEqualTo("테스트 옷");

      verify(recommendQueryRepository).findClothesRecommendations(testWeatherId, testUserId);
      verify(scoreCalculator).calculateScore(any(ScoredClothesDto.class));
      verify(clothesSelector).selectRecommendedClothes(anyList());
    }
  }

  @Test
  @DisplayName("추천 가능한 옷이 없을 때 빈 결과 반환 테스트")
  void recommend_EmptyResult() {
    // Given
    try (MockedStatic<SecurityContextHolder> securityContextHolderMock = 
         Mockito.mockStatic(SecurityContextHolder.class)) {
      
      securityContextHolderMock.when(SecurityContextHolder::getContext)
          .thenReturn(securityContext);
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getPrincipal()).thenReturn(userDetails);
      when(userDetails.getUser()).thenReturn(user);
      when(user.getId()).thenReturn(testUserId);

      when(recommendQueryRepository.findClothesRecommendations(testWeatherId, testUserId))
          .thenReturn(Collections.emptyList());

      // When
      RecommendationDto result = recommendService.recommend(testWeatherId);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.weatherId()).isEqualTo(testWeatherId);
      assertThat(result.userId()).isEqualTo(testUserId);
      assertThat(result.clothes()).isEmpty();

      verify(recommendQueryRepository).findClothesRecommendations(testWeatherId, testUserId);
    }
  }

  @Test
  @DisplayName("사용자 캐시 안전 삭제 테스트")
  void safeEvictUserCache_Success() {
    // Given
    when(cacheManager.getCache("recommendations")).thenReturn(cache);

    // When
    recommendService.safeEvictUserCache(testUserId);

    // Then
    verify(cacheManager).getCache("recommendations");
  }

  @Test
  @DisplayName("사용자 캐시 안전 삭제 - 캐시가 없는 경우")
  void safeEvictUserCache_NullCache() {
    // Given
    when(cacheManager.getCache("recommendations")).thenReturn(null);

    // When
    recommendService.safeEvictUserCache(testUserId);

    // Then
    verify(cacheManager).getCache("recommendations");
  }
}
