package com.example.ootd.domain.clothes.service.cache;

import com.example.ootd.domain.clothes.dto.data.ClothesDto;
import com.example.ootd.domain.clothes.dto.request.ClothesSearchCondition;
import com.example.ootd.domain.clothes.entity.Clothes;
import com.example.ootd.domain.clothes.entity.ClothesAttribute;
import com.example.ootd.domain.clothes.entity.ClothesType;
import com.example.ootd.domain.clothes.mapper.ClothesMapper;
import com.example.ootd.domain.clothes.repository.ClothesRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "clothes")
public class ClothesCacheService {

  private final ClothesRepository clothesRepository;
  private final ClothesMapper clothesMapper;
  private final CacheManager cacheManager;

  // 옷 조회 결과 캐시 저장 및 조회
  @Cacheable(key = "#condition.toSimpleKey()")
  public List<ClothesDto> getCachedClothes(ClothesSearchCondition condition) {

    log.debug("옷 캐시 MISS 발생 - condition={}", condition);

    saveKey(condition);

    List<Clothes> clothesList = clothesRepository.findByCondition(condition);
    List<UUID> clothesIds = clothesList.stream().map(Clothes::getId).toList();
    List<ClothesAttribute> clothesAttributeList = clothesRepository.findClothesAttributeByClothesIds(
        clothesIds);
    Map<UUID, List<ClothesAttribute>> clothesAttributeMap = clothesAttributeList.stream()
        .collect(Collectors.groupingBy(ca -> ca.getClothes().getId()));

    return clothesMapper.toDto(clothesList, clothesAttributeMap);
  }

  // 조건 해당 옷 개수 캐시 저장 및 조회
  @Cacheable(key = "#ownerId")
  public long getCachedTotalCount(ClothesType typeEqual, UUID ownerId) {

    log.debug("옷 개수 캐시 MISS 발생 - typeEqual={}, ownerId={}", typeEqual, ownerId);

    return clothesRepository.countByCondition(typeEqual, ownerId);
  }

  // 해당 유저의 옷 관련 캐시 모두 삭제
  public void deleteAllClothesCacheByOwnerId(UUID ownerId) {

    Set<String> keys = cacheManager.getCache("clothes_key").get(ownerId, Set.class);
    Cache clothesCache = cacheManager.getCache("clothes");

    if (clothesCache != null && keys != null) {
      for (String key : keys) {
        clothesCache.evict(key);
      }
      clothesCache.evict(ownerId);
    }

    cacheManager.getCache("feed_comment_key").evict(ownerId);
  }

  // clothes의 키들 저장
  private void saveKey(ClothesSearchCondition condition) {

    String key = condition.toSimpleKey();

    // 저장된 키 등록
    Set<String> existingKeys = cacheManager.getCache("clothes_key")
        .get(condition.ownerId(), Set.class);

    if (existingKeys == null) {
      existingKeys = new HashSet<>();
    }
    existingKeys.add(key);
    cacheManager.getCache("clothes_key").put(condition.ownerId(), existingKeys);
  }
}
