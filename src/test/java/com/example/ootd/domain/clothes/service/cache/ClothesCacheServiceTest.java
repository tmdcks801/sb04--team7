package com.example.ootd.domain.clothes.service.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.ootd.domain.clothes.dto.data.ClothesDto;
import com.example.ootd.domain.clothes.dto.request.ClothesSearchCondition;
import com.example.ootd.domain.clothes.entity.Clothes;
import com.example.ootd.domain.clothes.entity.ClothesType;
import com.example.ootd.domain.clothes.mapper.ClothesMapper;
import com.example.ootd.domain.clothes.repository.ClothesRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@ExtendWith(MockitoExtension.class)
public class ClothesCacheServiceTest {

  @Mock
  private ClothesRepository clothesRepository;
  @Mock
  private ClothesMapper clothesMapper;
  @Mock
  private CacheManager cacheManager;
  @Mock
  private Cache clothesCache;
  @Mock
  private Cache clothesKeyCache;

  @InjectMocks
  private ClothesCacheService clothesCacheService;

  private UUID ownerId;
  private ClothesSearchCondition condition;

  @BeforeEach
  void setUp() {
    ownerId = UUID.randomUUID();
    condition = ClothesSearchCondition.builder()
        .ownerId(ownerId)
        .limit(5)
        .build();
  }

  @Test
  @DisplayName("getCachedClothes - 캐시 MISS 시 repository와 mapper가 호출된다")
  void getCachedClothes_miss() {
    // given
    UUID clothesId = UUID.randomUUID();
    Clothes clothes = Clothes.builder().name("상의").type(ClothesType.TOP).build();

    List<Clothes> clothesList = List.of(clothes);
    List<ClothesDto> dtoList = List.of(
        new ClothesDto(clothesId, ownerId, "상의", null, ClothesType.TOP, new ArrayList<>(),
            LocalDateTime.now()));

    given(clothesRepository.findByCondition(eq(condition))).willReturn(clothesList);
    given(clothesRepository.findClothesAttributeByClothesIds(any())).willReturn(new ArrayList<>());
    given(clothesMapper.toDto(anyList(), anyMap()))
        .willReturn(dtoList);
    given(cacheManager.getCache("clothes_key")).willReturn(clothesKeyCache);

    // when
    List<ClothesDto> result = clothesCacheService.getCachedClothes(condition);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).name()).isEqualTo("상의");
    verify(clothesRepository, times(1)).findByCondition(eq(condition));
    verify(clothesRepository, times(1)).findClothesAttributeByClothesIds(any());
    verify(clothesMapper, times(1)).toDto(anyList(), anyMap());
    verify(clothesKeyCache, times(1)).put(eq(ownerId), any(Set.class));
  }

  @Test
  @DisplayName("getCachedTotalCount - 캐시 MISS 시 repository count 호출")
  void getCachedTotalCount_miss() {
    // given
    given(clothesRepository.countByCondition(ClothesType.TOP, ownerId)).willReturn(3L);

    // when
    long count = clothesCacheService.getCachedTotalCount(ClothesType.TOP, ownerId);

    // then
    assertThat(count).isEqualTo(3L);
    verify(clothesRepository, times(1)).countByCondition(ClothesType.TOP, ownerId);
  }

  @Test
  @DisplayName("deleteAllClothesCacheByOwnerId - 캐시 키 삭제가 정상적으로 호출된다")
  void deleteAllClothesCacheByOwnerId() {
    // given
    Set<String> keys = Set.of("key1", "key2");
    given(cacheManager.getCache("clothes")).willReturn(clothesCache);
    given(cacheManager.getCache("clothes_key")).willReturn(clothesKeyCache);
    given(clothesKeyCache.get(ownerId, Set.class)).willReturn(keys);

    // when
    clothesCacheService.deleteAllClothesCacheByOwnerId(ownerId);

    // then
    verify(clothesCache, times(1)).evict("key1");
    verify(clothesCache, times(1)).evict("key2");
    verify(clothesCache, times(1)).evict(ownerId);
    verify(clothesKeyCache, times(1)).evict(ownerId);
  }
}
