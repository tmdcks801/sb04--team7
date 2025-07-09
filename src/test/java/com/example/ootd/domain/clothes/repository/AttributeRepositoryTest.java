package com.example.ootd.domain.clothes.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ootd.config.QueryDslConfig;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeSearchCondition;
import com.example.ootd.domain.clothes.entity.Attribute;
import com.example.ootd.domain.clothes.entity.Clothes;
import com.example.ootd.domain.image.entity.Image;
import com.example.ootd.domain.image.service.S3Service;
import com.example.ootd.domain.location.Location;
import com.example.ootd.domain.user.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
@EntityScan(basePackageClasses = {Attribute.class, Clothes.class, Image.class, User.class,
    Location.class})  // 간접 연관관계로 인해 복잡해짐
@EnableJpaRepositories(basePackageClasses = AttributeRepository.class)
public class AttributeRepositoryTest {

  @MockitoBean
  private S3Service s3Service;  // image와 연관되어 있어 필요

  @Autowired
  private AttributeRepository attributeRepository;

  private final List<Attribute> list = new ArrayList<>();

  @BeforeEach
  void setUp() {

    list.clear();

    // 테스트용 데이터 저장
    for (int i = 0; i < 10; i++) {
      Attribute attribute = Attribute.builder().name("테스트" + i)
          .details(new ArrayList<>(Arrays.asList(String.valueOf(i), "test" + i))).build();
      list.add(attribute);
    }
    for (int i = 0; i < 5; i++) {
      Attribute attribute = Attribute.builder().name("테스트" + i + i)
          .details(new ArrayList<>(Arrays.asList(String.valueOf(i), "test" + i + i))).build();
      list.add(attribute);
    }
    attributeRepository.saveAll(list);
  }

  @Nested
  @DisplayName("findByCondition() - 검색조건에 맞는 속성 조회")
  class findByConditionTest {

    @Test
    @DisplayName("성공 - 기본요건만 있는 경우, 이름 오름차순")
    void findByConditionSuccessNameAsc() {

      // given
      ClothesAttributeSearchCondition condition = ClothesAttributeSearchCondition.builder().limit(5)
          .sortBy("name").sortDirection("ASCENDING").build();
      list.sort(Comparator.comparing(Attribute::getName));  // list 이름 오름차순으로 정렬

      // when
      List<Attribute> result = attributeRepository.findByCondition(condition);

      // then
      assertThat(result).hasSize(condition.limit() + 1);
      assertThat(result.get(condition.limit() - 1).getName()).isEqualTo(
          list.get(condition.limit() - 1).getName());
      assertThat(result.get(0).getName()).isEqualTo("테스트0");
    }

    @Test
    @DisplayName("성공 - 기본요건만 있는 경우, 이름 내림차순")
    void findByConditionSuccessNameDesc() {

      // given
      ClothesAttributeSearchCondition condition = ClothesAttributeSearchCondition.builder().limit(5)
          .sortBy("name").sortDirection("DESCENDING").build();
      list.sort(Comparator.comparing(Attribute::getName).reversed());  // list 이름 내림차순 정렬

      // when
      List<Attribute> result = attributeRepository.findByCondition(condition);

      // then
      assertThat(result).hasSize(condition.limit() + 1);
      assertThat(result.get(condition.limit() - 1).getName()).isEqualTo(
          list.get(condition.limit() - 1).getName());
      assertThat(result.get(0).getName()).isEqualTo("테스트9");
    }

    @Test
    @DisplayName("성공 - 기본요건만 있는 경우, 생성일 오름차순")
    void findByConditionSuccessCreatedAtAsc() {

      // given
      ClothesAttributeSearchCondition condition = ClothesAttributeSearchCondition.builder().limit(5)
          .sortBy("createdAt").sortDirection("ASCENDING").build();
      list.sort(Comparator.comparing(Attribute::getCreatedAt) // list 생성일 오름차순 정렬
          .thenComparing(Attribute::getId));   // 생성일 같을 경우 id 오름차순 정렬

      // when
      List<Attribute> result = attributeRepository.findByCondition(condition);

      // then
      assertThat(result).hasSize(condition.limit() + 1);
      assertThat(result.get(condition.limit() - 1).getName()).isEqualTo(
          list.get(condition.limit() - 1).getName());
    }

    @Test
    @DisplayName("성공 - 기본요건만 있는 경우, 생성일 내림차순")
    void findByConditionSuccessCreatedAtDesc() {

      // given
      ClothesAttributeSearchCondition condition = ClothesAttributeSearchCondition.builder().limit(5)
          .sortBy("createdAt").sortDirection("DESCENDING").build();
      list.sort(Comparator.comparing(Attribute::getCreatedAt).reversed()  // list 생성일 내림차순 정렬
          .thenComparing(Attribute::getId));  // 생성일 같을 경우 id 오름차순 정렬

      // when
      List<Attribute> result = attributeRepository.findByCondition(condition);

      // then
      assertThat(result).hasSize(condition.limit() + 1);
      assertThat(result.get(condition.limit() - 1).getName()).isEqualTo(
          list.get(condition.limit() - 1).getName());
    }
  }
}
