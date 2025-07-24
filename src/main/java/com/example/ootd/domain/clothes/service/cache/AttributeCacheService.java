package com.example.ootd.domain.clothes.service.cache;

import com.example.ootd.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeSearchCondition;
import com.example.ootd.domain.clothes.entity.Attribute;
import com.example.ootd.domain.clothes.mapper.AttributeMapper;
import com.example.ootd.domain.clothes.repository.AttributeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "attribute")
public class AttributeCacheService {

  private final AttributeRepository attributeRepository;
  private final AttributeMapper attributeMapper;
  private final CacheManager cacheManager;

  // 속성 조회 결과 캐시 저장 및 조회
  @Cacheable(key = "#condition.toSimpleKey()")
  public List<ClothesAttributeDefDto> getCachedClothes(ClothesAttributeSearchCondition condition) {

    log.debug("속성 캐시 MISS 발생 - condition={}", condition);

    List<Attribute> attributeList = attributeRepository.findByCondition(condition);

    return attributeMapper.toDtoList(attributeList);
  }

  // 조건 해당 속성 개수 캐시 저장 및 조회
  @Cacheable(key = "#keywordLike != null ? #keywordLike : 'ALL'")
  public long getCachedTotalCount(String keywordLike) {

    log.debug("속성 개수 캐시 MISS 발생 - keyword={}", keywordLike);

    return attributeRepository.countByKeyword(keywordLike);
  }

  // 모든 캐시 삭제
  @CacheEvict(allEntries = true)
  public void evictAllCache() {
    log.info("Attribute 캐시 전체 삭제");
  }
}
