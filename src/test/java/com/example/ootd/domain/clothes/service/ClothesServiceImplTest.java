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
import com.example.ootd.exception.user.UserIdNotFoundException;
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
}
