package com.example.ootd.domain.clothes.repository;


import static org.assertj.core.api.Assertions.assertThat;

import com.example.ootd.TestEntityFactory;
import com.example.ootd.config.JpaConfig;
import com.example.ootd.config.QueryDslConfig;
import com.example.ootd.domain.clothes.dto.request.ClothesSearchCondition;
import com.example.ootd.domain.clothes.entity.Attribute;
import com.example.ootd.domain.clothes.entity.Clothes;
import com.example.ootd.domain.clothes.entity.ClothesAttribute;
import com.example.ootd.domain.clothes.entity.ClothesType;
import com.example.ootd.domain.image.entity.Image;
import com.example.ootd.domain.image.repository.ImageRepository;
import com.example.ootd.domain.image.service.S3Service;
import com.example.ootd.domain.location.Location;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
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
@Import({QueryDslConfig.class, JpaConfig.class})
@ActiveProfiles("test")
@EntityScan(basePackageClasses = {Attribute.class, Clothes.class, Image.class, User.class,
    Location.class})
@EnableJpaRepositories(basePackageClasses = {ClothesRepository.class, ImageRepository.class,
    UserRepository.class})
public class ClothesRepositoryTest {

  @MockitoBean
  private S3Service s3Service;  // image와 연관되어 있어 필요

  @Autowired
  private ClothesRepository clothesRepository;
  @Autowired
  private ImageRepository imageRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private AttributeRepository attributeRepository;


  private final List<Clothes> list = new ArrayList<>();
  private User user1;
  private Attribute colorAttr;
  private Attribute sizeAttr;

  @BeforeEach
  void setUp() {

    list.clear();
    user1 = userRepository.save(TestEntityFactory.createUserWithoutId("1"));
    User user2 = userRepository.save(TestEntityFactory.createUserWithoutId("2"));

    // 테스트용 데이터 저장
    for (int i = 0; i < 6; i++) {
      Image image = imageRepository.save(TestEntityFactory.createImageWithoutId(String.valueOf(i)));
      Clothes clothes = Clothes.builder().user(user1).image(image)
          .name("test" + i).type(ClothesType.values()[i]).build();
      list.add(clothes);
    }
    for (int i = 0; i < 3; i++) {
      Clothes clothes = Clothes.builder().user(user1)
          .name("test" + i + i).type(ClothesType.values()[i]).build();
      list.add(clothes);
    }
    for (int i = 0; i < 3; i++) {
      Clothes clothes = Clothes.builder().user(user2)
          .name("user2-test" + i + i).type(ClothesType.values()[i]).build();
      list.add(clothes);
    }

    // Attribute 2개
    colorAttr = Attribute.builder().name("색상").details(List.of("검정", "흰색")).build();
    sizeAttr = Attribute.builder().name("사이즈").details(List.of("M", "L")).build();
    attributeRepository.saveAll(List.of(colorAttr, sizeAttr));

    // ClothesAttribute 매핑
    ClothesAttribute clothesAttribute1 = new ClothesAttribute(list.get(0), colorAttr, "검정");
    ClothesAttribute clothesAttribute2 = new ClothesAttribute(list.get(0), sizeAttr, "M");
    ClothesAttribute clothesAttribute3 = new ClothesAttribute(list.get(1), colorAttr, "검정");

    list.get(0).addClothesAttribute(clothesAttribute1);
    list.get(0).addClothesAttribute(clothesAttribute2);
    list.get(1).addClothesAttribute(clothesAttribute3);

    clothesRepository.saveAll(list);
  }

  @Nested
  @DisplayName("findByCondition() - 검색 조건에 맞는 옷 조회")
  class FindByConditionTest {

    @Test
    @DisplayName("성공 - 기본 요건(limit, ownerId)만 있는 경우")
    void findByCondition_success() {

      // given
      int limit = 5;
      ClothesSearchCondition condition = ClothesSearchCondition.builder()
          .limit(limit).ownerId(user1.getId()).build();
      List<Clothes> filteredList = list.stream()
          .filter(c -> c.getUser().getId().equals(user1.getId()))
          .sorted(Comparator.comparing(Clothes::getCreatedAt, Comparator.reverseOrder())
              .thenComparing(c -> c.getId().toString(), Comparator.reverseOrder()))
          .toList();

      // when
      List<Clothes> result = clothesRepository.findByCondition(condition);

      // then
      assertThat(result).hasSize(limit + 1);
      assertThat(result.get(0)).isEqualTo(filteredList.get(0));
      assertThat(result.get(limit)).isEqualTo(filteredList.get(limit));
      assertThat(result).containsExactlyElementsOf(filteredList.subList(0, limit + 1));
    }

    @Test
    @DisplayName("성공 - 기본요건 + type 작성하는 경우")
    void findByCondition_success_type() {

      // given
      int limit = 5;
      ClothesSearchCondition condition = ClothesSearchCondition.builder()
          .limit(limit).ownerId(user1.getId()).typeEqual(ClothesType.OUTER).build();
      List<Clothes> filteredList = list.stream()
          .filter(c -> c.getUser().getId().equals(user1.getId())
              && c.getType().equals(ClothesType.OUTER))
          .sorted(Comparator.comparing(Clothes::getCreatedAt, Comparator.reverseOrder())
              .thenComparing(c -> c.getId().toString(), Comparator.reverseOrder()))
          .toList();

      // when
      List<Clothes> result = clothesRepository.findByCondition(condition);

      // then
      assertThat(result).hasSize(filteredList.size());
      assertThat(result).containsExactlyElementsOf(filteredList);
    }

//    @Test
//    @DisplayName("성공 - 커서 페이지네이션")
//    void findByCondition_success_cursor() {
//
//      // given
//      int limit = 3;
//      List<Clothes> filteredList = list.stream()
//          .filter(c -> c.getUser().getId().equals(user1.getId()))
//          .sorted(Comparator.comparing(Clothes::getCreatedAt, Comparator.reverseOrder())
//              .thenComparing(c -> c.getId().toString(), Comparator.reverseOrder()))
//          .toList();
//      ClothesSearchCondition condition = ClothesSearchCondition.builder()
//          .limit(limit).ownerId(user1.getId()).idAfter(filteredList.get(limit - 1).getId())
//          .cursor(filteredList.get(limit - 1).getCreatedAt().toString()).build();
//
//      // when
//      List<Clothes> result = clothesRepository.findByCondition(condition);
//
//      // then
//      assertThat(result).hasSize(limit + 1);
//      assertThat(result.get(0)).isEqualTo(filteredList.get(limit));
//    }
  }

  @Nested
  @DisplayName("findClothesAttributeByClothesIds() - 옷들의 속성 리스트 조회")
  class FindClothesAttributeByClothesIdsTest {

    @Test
    @DisplayName("성공 - 여러 옷 ID로 속성 리스트 조회")
    void findClothesAttributeByClothesIds_success() {
      // given
      List<UUID> ids = List.of(list.get(0).getId(), list.get(1).getId());

      // when
      var result = clothesRepository.findClothesAttributeByClothesIds(ids);

      // then
      assertThat(result).hasSize(3);
      assertThat(result)
          .extracting(ca -> ca.getAttribute().getName())
          .containsExactlyInAnyOrder("색상", "사이즈", "색상");
    }
  }

  @Nested
  @DisplayName("findClothesAttributeByClothesId() - 옷의 속성 리스트 조회")
  class FindClothesAttributeByClothesIdTest {

    @Test
    @DisplayName("성공 - 단일 옷 ID로 속성 리스트 조회")
    void findClothesAttributeByClothesId_success() {
      // when
      var result = clothesRepository.findClothesAttributeByClothesId(list.get(0).getId());

      // then
      assertThat(result).hasSize(2);
      assertThat(result)
          .extracting(ca -> ca.getAttribute().getName())
          .containsExactlyInAnyOrder("색상", "사이즈");
    }
  }

  @Nested
  @DisplayName("countByCondition() - 검색 조건에 맞는 옷 개수")
  class CountByConditionTest {

    @Test
    @DisplayName("성공 - 타입, 사용자 id 입력 시 해당되는 옷 개수 반환")
    void countByCondition_success() {

      // given
      ClothesType type = ClothesType.BOTTOM;

      // when
      long count = clothesRepository.countByCondition(type, user1.getId());

      // then
      assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("성공 - 조건에 해당되는 옷이 없는 경우 0 반환")
    void countByCondition_success_null() {

      // given
      ClothesType type = ClothesType.ETC;

      // when
      long count = clothesRepository.countByCondition(type, user1.getId());

      // then
      assertThat(count).isEqualTo(0);
    }
  }
}