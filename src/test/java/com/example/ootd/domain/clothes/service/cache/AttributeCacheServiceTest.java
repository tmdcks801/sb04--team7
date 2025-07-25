package com.example.ootd.domain.clothes.service.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.ootd.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeSearchCondition;
import com.example.ootd.domain.clothes.entity.Attribute;
import com.example.ootd.domain.clothes.mapper.AttributeMapper;
import com.example.ootd.domain.clothes.repository.AttributeRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;

@ExtendWith(MockitoExtension.class)
public class AttributeCacheServiceTest {

  @Mock
  private AttributeRepository attributeRepository;
  @Mock
  private AttributeMapper attributeMapper;
  @Mock
  private Cache cache;

  @InjectMocks
  private AttributeCacheService attributeCacheService;

  @Test
  @DisplayName("getCachedClothes - 캐시 MISS 시 repository와 mapper가 호출된다")
  void getCachedClothes_miss() {
    // given
    ClothesAttributeSearchCondition condition = ClothesAttributeSearchCondition.builder()
        .keywordLike("상의")
        .build();

    List<Attribute> attributeList = List.of(new Attribute("색상", List.of("검정", "흰색")));
    List<ClothesAttributeDefDto> dtoList = List.of(
        new ClothesAttributeDefDto(UUID.randomUUID(), "색상", List.of("검정", "흰색"),
            LocalDateTime.now()));

    given(attributeRepository.findByCondition(eq(condition))).willReturn(attributeList);
    given(attributeMapper.toDtoList(eq(attributeList))).willReturn(dtoList);

    // when
    List<ClothesAttributeDefDto> result = attributeCacheService.getCachedClothes(condition);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).name()).isEqualTo("색상");
    verify(attributeRepository, times(1)).findByCondition(eq(condition));
    verify(attributeMapper, times(1)).toDtoList(eq(attributeList));
  }

  @Test
  @DisplayName("getCachedTotalCount - 캐시 MISS 시 repository count 호출")
  void getCachedTotalCount_miss() {
    // given
    String keyword = "상의";
    given(attributeRepository.countByKeyword(keyword)).willReturn(5L);

    // when
    long count = attributeCacheService.getCachedTotalCount(keyword);

    // then
    assertThat(count).isEqualTo(5L);
    verify(attributeRepository, times(1)).countByKeyword(keyword);
  }
}
