package com.example.ootd.domain.clothes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.ootd.TestEntityFactory;
import com.example.ootd.domain.clothes.dto.data.ClothesAttributeDto;
import com.example.ootd.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.example.ootd.domain.clothes.dto.data.ClothesDto;
import com.example.ootd.domain.clothes.dto.request.ClothesCreateRequest;
import com.example.ootd.domain.clothes.dto.request.ClothesSearchCondition;
import com.example.ootd.domain.clothes.dto.request.ClothesUpdateRequest;
import com.example.ootd.domain.clothes.entity.Attribute;
import com.example.ootd.domain.clothes.entity.Clothes;
import com.example.ootd.domain.clothes.entity.ClothesType;
import com.example.ootd.domain.clothes.mapper.ClothesMapper;
import com.example.ootd.domain.clothes.repository.AttributeRepository;
import com.example.ootd.domain.clothes.repository.ClothesRepository;
import com.example.ootd.domain.clothes.service.impl.ClothesServiceImpl;
import com.example.ootd.domain.image.entity.Image;
import com.example.ootd.domain.image.service.ImageService;
import com.example.ootd.domain.recommend.service.RecommendService;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.dto.PageResponse;
import com.example.ootd.exception.clothes.ClothesNotFountException;
import com.example.ootd.exception.user.UserIdNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class ClothesServiceImplTest {

  @Mock
  private ImageService imageService;
  @Mock
  private ClothesRepository clothesRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private AttributeRepository attributeRepository;
  @Mock
  private ClothesMapper clothesMapper;
  @Mock
  private RecommendService recommendService;

  @InjectMocks
  private ClothesServiceImpl clothesService;

  private final List<Clothes> clothesList = new ArrayList<>();
  private final User user = TestEntityFactory.createUser();
  private final Image image = TestEntityFactory.createImage();
  private final Attribute attribute = TestEntityFactory.createAttribute();
  private final MultipartFile file = new MockMultipartFile("test",
      "test.jpg", "image/jpeg", "dummy image content".getBytes());

  @BeforeEach
  void setUp() {

    for (int i = 1; i <= 5; i++) {
      Clothes clothes = Clothes.builder().user(user).image(image)
          .name("옷" + i).type(ClothesType.values()[i]).build();
      ReflectionTestUtils.setField(clothes, "id", UUID.randomUUID());
      ReflectionTestUtils.setField(clothes, "createdAt", LocalDateTime.now());
      clothesList.add(clothes);
    }
  }

  @Nested
  @DisplayName("create() - 의상 등록")
  class CreateTest {

    @Test
    @DisplayName("성공 - 등록 성공")
    void create_success() {

      // given
      ClothesAttributeDto clothesAttributeDto = new ClothesAttributeDto(
          attribute.getId(), attribute.getDetails().get(0)
      );
      ClothesCreateRequest request = new ClothesCreateRequest(
          user.getId(), "등록 테스트", ClothesType.ETC, List.of(clothesAttributeDto)
      );
      Clothes clothes = new Clothes(user, image, request.name(), request.type());
      ClothesAttributeWithDefDto clothesAttributeWithDefDto = new ClothesAttributeWithDefDto(
          attribute.getId(), attribute.getName(), attribute.getDetails(),
          "test1"
      );
      ClothesDto clothesDto = ClothesDto.builder().id(UUID.randomUUID()).ownerId(user.getId())
          .name(clothes.getName()).imageUrl(image.getUrl()).type(clothes.getType())
          .attributes(List.of(clothesAttributeWithDefDto)).build();
      given(imageService.upload(any(MultipartFile.class))).willReturn(image);
      given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(user));
      given(clothesMapper.toDto(any(Clothes.class))).willReturn(clothesDto);
      given(attributeRepository.findAllById(any())).willReturn(List.of(attribute));

      // when
      ClothesDto result = clothesService.create(request, file, user.getId());

      // then
      assertThat(result.name()).isEqualTo(clothes.getName());
      verify(imageService).upload(file);
      verify(clothesRepository).save(any(Clothes.class));
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 사용자일 때 예외처리")
    void create_fail_user_not_found() {

      // given
      ClothesAttributeDto clothesAttributeDto = new ClothesAttributeDto(
          attribute.getId(), attribute.getDetails().get(0)
      );
      ClothesCreateRequest request = new ClothesCreateRequest(
          UUID.randomUUID(), "등록 테스트", ClothesType.ETC, List.of(clothesAttributeDto)
      );
      given(imageService.upload(any(MultipartFile.class))).willReturn(image);
      given(userRepository.findById(any(UUID.class))).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> clothesService.create(request, file, request.ownerId()))
          .isInstanceOf(UserIdNotFoundException.class);
      verify(userRepository).findById(request.ownerId());
      verify(clothesRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("update() - 의상 수정")
  class UpdateTest {

    @Test
    @DisplayName("성공 - 수정 성공")
    void update_success() {

      // given
      UUID clothesId = clothesList.get(0).getId();
      ClothesAttributeDto updatedAttributeDto = new ClothesAttributeDto(
          attribute.getId(), attribute.getDetails().get(0)
      );
      Clothes existingClothes = clothesList.get(0);
      ClothesType newType = ClothesType.ETC;
      String newName = "수정된 이름";
      ClothesUpdateRequest request = new ClothesUpdateRequest(
          newName, newType, List.of(updatedAttributeDto)
      );
      MultipartFile newImageFile = new MockMultipartFile("image", "new.jpg", "image/jpeg",
          "new image content".getBytes());
      Image newImage = TestEntityFactory.createImage();

      ClothesDto updatedClothesDto = ClothesDto.builder()
          .id(clothesId)
          .ownerId(user.getId())
          .name(newName)
          .type(newType)
          .imageUrl(newImage.getUrl())
          .attributes(List.of()) // 필요한 경우 실제 attribute DTO 넣기
          .build();
      given(clothesRepository.findById(clothesId)).willReturn(Optional.of(existingClothes));
      given(imageService.upload(newImageFile)).willReturn(newImage);
      given(attributeRepository.findById(attribute.getId())).willReturn(Optional.of(attribute));
      given(clothesMapper.toDto(existingClothes)).willReturn(updatedClothesDto);

      // when
      ClothesDto result = clothesService.update(request, newImageFile, clothesId);

      // then
      assertThat(result).isNotNull();
      assertThat(result.name()).isEqualTo(newName);
      assertThat(result.type()).isEqualTo(newType);
      verify(clothesRepository).findById(clothesId);
      verify(imageService).upload(newImageFile);
      verify(recommendService).safeEvictUserCache(user.getId());
      assertThat(existingClothes.getName()).isEqualTo(newName);
      assertThat(existingClothes.getType()).isEqualTo(newType);
      assertThat(existingClothes.getImage()).isEqualTo(newImage);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 옷일 때 예외처리")
    void update_fail_not_found() {

      // given
      UUID clothesId = UUID.randomUUID();
      ClothesUpdateRequest request = new ClothesUpdateRequest(
          null, null, null
      );

      // when & then
      assertThatThrownBy(() -> clothesService.update(request, null, clothesId))
          .isInstanceOf(ClothesNotFountException.class);
      verify(clothesRepository).findById(clothesId);
      verify(clothesMapper, never()).toDto(any(Clothes.class));
    }
  }

  @Nested
  @DisplayName("delete() - 의상 삭제")
  class DeleteTest {

    @Test
    @DisplayName("성공 - 삭제 성공")
    void delete_success() {
      // given
      UUID clothesId = clothesList.get(0).getId();
      Clothes clothes = clothesList.get(0);

      given(clothesRepository.findById(clothesId)).willReturn(Optional.of(clothes));

      // when
      clothesService.delete(clothesId);

      // then
      verify(clothesRepository).delete(clothes);
      verify(recommendService).safeEvictUserCache(user.getId());
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 의상일 경우 예외 처리")
    void delete_fail_not_found() {
      // given
      UUID invalidId = UUID.randomUUID();
      given(clothesRepository.findById(invalidId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> clothesService.delete(invalidId))
          .isInstanceOf(ClothesNotFountException.class);

      verify(clothesRepository).findById(invalidId);
      verify(clothesRepository, never()).delete(any());
      verify(recommendService, never()).safeEvictUserCache(any());
    }
  }

  @Nested
  @DisplayName("findByCondition() - 의상 조회")
  class FindByConditionTest {

    @Test
    @DisplayName("성공 - 의상 목록 조회 및 페이징 정보 포함")
    void findByCondition_success() {

      // given
      int limit = 3;

      ClothesSearchCondition condition = ClothesSearchCondition.builder()
          .limit(limit)
          .typeEqual(ClothesType.TOP)
          .ownerId(user.getId())
          .build();

      // DTO 매핑 후 결과
      List<ClothesDto> dtoList = clothesList.subList(0, 3).stream()
          .map(c -> ClothesDto.builder()
              .id(UUID.randomUUID())
              .ownerId(user.getId())
              .name(c.getName())
              .type(c.getType())
              .imageUrl(c.getImage().getUrl())
              .attributes(List.of())
              .build())
          .toList();

      given(clothesRepository.findByCondition(condition)).willReturn(clothesList);
      given(clothesRepository.countByCondition(condition.typeEqual(), condition.ownerId()))
          .willReturn(10L);
      given(clothesMapper.toDto(any(List.class))).willReturn(dtoList);

      // when
      PageResponse<ClothesDto> result = clothesService.findByCondition(condition);

      // then
      assertThat(result.data()).hasSize(3); // 실제 반환은 limit
      assertThat(result.hasNext()).isTrue();
      assertThat(result.totalCount()).isEqualTo(10L);
      assertThat(result.sortBy()).isEqualTo("createdAt");
      assertThat(result.sortDirection()).isEqualTo("DESCENDING");

      verify(clothesRepository).findByCondition(condition);
      verify(clothesRepository).countByCondition(condition.typeEqual(), condition.ownerId());
      verify(clothesMapper).toDto(any(List.class));
    }
  }

}
