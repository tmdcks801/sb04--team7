package com.example.ootd.domain.clothes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.ootd.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeSearchCondition;
import com.example.ootd.domain.clothes.entity.Attribute;
import com.example.ootd.domain.clothes.mapper.AttributeMapper;
import com.example.ootd.domain.clothes.repository.AttributeRepository;
import com.example.ootd.domain.clothes.service.impl.AttributeServiceImpl;
import com.example.ootd.domain.notification.dto.NotificationEvent;
import com.example.ootd.domain.notification.service.inter.NotificationPublisherInterface;
import com.example.ootd.dto.PageResponse;
import com.example.ootd.exception.clothes.AttributeNameAlreadyExistsException;
import com.example.ootd.exception.clothes.AttributeNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class AttributeServiceImplTest {

  @Mock
  private AttributeRepository attributeRepository;
  @Mock
  private AttributeMapper attributeMapper;
  @Mock
  private NotificationPublisherInterface notificationPublisher;

  @InjectMocks
  private AttributeServiceImpl attributeService;

  private final List<Attribute> attributes = new ArrayList<>();

  @BeforeEach
  void setUp() {
    for (int i = 1; i <= 10; i++) {
      Attribute attribute = new Attribute(
          "test" + i, List.of("test", "test" + i)
      );
      ReflectionTestUtils.setField(attribute, "id", UUID.randomUUID());
      attributes.add(attribute);
    }
  }

  @Nested
  @DisplayName("create() - 의상 속성 정의 등록")
  class CreateTest {

    @Test
    @DisplayName("성공 - 등록 성공")
    void create_success() {

      // given
      ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest(
          "테스트", List.of("test1", "test2")
      );
      ClothesAttributeDefDto dto = ClothesAttributeDefDto.builder().id(UUID.randomUUID())
          .name(request.name())
          .selectableValues(request.selectableValues()).build();
      given(attributeRepository.existsByName(request.name())).willReturn(false);
      given(attributeMapper.toDto(any(Attribute.class))).willReturn(dto);

      // when
      ClothesAttributeDefDto result = attributeService.create(request);

      // then
      ArgumentCaptor<Attribute> captor = ArgumentCaptor.forClass(Attribute.class);
      verify(attributeRepository).save(captor.capture());
      Attribute saved = captor.getValue();
      assertThat(saved.getName()).isEqualTo("테스트");
      assertThat(saved.getDetails()).containsExactlyInAnyOrder("test1", "test2");

      assertThat(result).isEqualTo(dto);
      verify(attributeRepository).existsByName(any(String.class));
      verify(attributeRepository).save(any(Attribute.class));
      verify(attributeMapper).toDto(any(Attribute.class));
      verify(notificationPublisher).publishToAll(any(NotificationEvent.class));
    }

    @Test
    @DisplayName("실패 - 이미 있는 속성의 경우 예외처리")
    void create_fail_duplicate() {

      // given
      ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest(
          "등록 테스트", List.of("test1", "test2")
      );
      given(attributeRepository.existsByName(request.name())).willReturn(true);

      // when & then
      assertThatThrownBy(() -> attributeService.create(request))
          .isInstanceOf(AttributeNameAlreadyExistsException.class);
      verify(attributeRepository).existsByName(request.name());
      verify(attributeRepository, never()).save(any());
      verify(notificationPublisher, never()).publishToAll(any());
    }
  }

  @Nested
  @DisplayName("update() - 의상 속성 정의 수정")
  class UpdateTest {

    @Test
    @DisplayName("성공 - 수정 성공")
    void update_success() {
      // given
      ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest(
          "수정 테스트", List.of("test1", "test2", "test3")
      );
      Attribute attribute = attributes.get(0);
      ClothesAttributeDefDto dto = ClothesAttributeDefDto.builder()
          .id(attribute.getId()).name(request.name())
          .selectableValues(request.selectableValues()).build();
      given(attributeRepository.findById(attribute.getId())).willReturn(
          Optional.of(attributes.get(0)));
      given(attributeMapper.toDto(any(Attribute.class))).willReturn(dto);

      // when
      ClothesAttributeDefDto result = attributeService.update(request, attribute.getId());

      // then
      assertThat(attribute.getName()).isEqualTo(request.name());
      assertThat(attribute.getDetails()).isEqualTo(request.selectableValues());
      verify(notificationPublisher).publishToAll(any(NotificationEvent.class));
      verify(attributeMapper).toDto(attribute);
      assertThat(result.name()).isEqualTo("수정 테스트");
      assertThat(result.selectableValues()).containsExactly("test1", "test2", "test3");
    }

    @Test
    @DisplayName("실패 - 존재하지 않은 속성일 경우 예외처리")
    void update_fail_not_found() {

      // given
      ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest(
          "수정 테스트", List.of("test1", "test2", "test3")
      );
      given(attributeRepository.findById(any(UUID.class))).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> attributeService.update(request, UUID.randomUUID()))
          .isInstanceOf(AttributeNotFoundException.class);
      verify(notificationPublisher, never()).publishToAll(any());
    }
  }

  @Nested
  @DisplayName("delete() - 의상 속성 정의 삭제")
  class DeleteTest {

    @Test
    @DisplayName("성공 - 삭제 성공")
    void delete_success() {

      // given
      Attribute attribute = attributes.get(0);
      given(attributeRepository.findById(attribute.getId())).willReturn(Optional.of(attribute));

      // when
      attributeService.delete(attribute.getId());

      // then
      verify(attributeRepository).delete(attribute);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 속성일 경우 예외처리")
    void delete_fail_not_found() {

      // given
      given(attributeRepository.findById(any(UUID.class))).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> attributeService.delete(UUID.randomUUID()))
          .isInstanceOf(AttributeNotFoundException.class);
      verify(attributeRepository, never()).delete(any(Attribute.class));
    }
  }

  @Nested
  @DisplayName("findByCondition() - 의상 속성 정의 목록 조회")
  class FindByConditionTest {

    @Test
    @DisplayName("성공 - 다음 페이지가 존재할 때")
    void findByCondition_success_has_next_page() {

      // given
      ClothesAttributeSearchCondition condition = ClothesAttributeSearchCondition.builder()
          .limit(5).sortBy("name").sortDirection("ASCENDING").build();
      given(attributeRepository.findByCondition(condition)).willReturn(attributes.subList(0, 6));
      given(attributeRepository.countByKeyword(condition.keywordLike())).willReturn(
          (long) attributes.size());
      given(attributeMapper.toDtoList(any())).willReturn(
          attributes.subList(0, 5).stream()
              .map(attr -> new ClothesAttributeDefDto(attr.getId(), attr.getName(),
                  attr.getDetails()))
              .collect(Collectors.toList())
      );

      // when
      PageResponse<ClothesAttributeDefDto> result = attributeService.findByCondition(condition);

      // then
      assertThat(result.hasNext()).isTrue();
      assertThat(result.nextCursor()).isEqualTo(attributes.get(4).getName());
      assertThat(result.data()).hasSize(5);
      verify(attributeRepository).findByCondition(condition);
      verify(attributeRepository).countByKeyword(condition.keywordLike());
    }

    @Test
    @DisplayName("성공 - 다음 페이지가 존재하지 않을 때")
    void findByCondition_success_has_not_next_page() {

      // given
      ClothesAttributeSearchCondition condition = ClothesAttributeSearchCondition.builder()
          .limit(20).sortBy("name").sortDirection("ASCENDING").build();
      given(attributeRepository.findByCondition(condition)).willReturn(attributes);
      given(attributeRepository.countByKeyword(condition.keywordLike())).willReturn(
          (long) attributes.size());
      given(attributeMapper.toDtoList(any())).willReturn(
          attributes.stream()
              .map(attr -> new ClothesAttributeDefDto(attr.getId(), attr.getName(),
                  attr.getDetails()))
              .collect(Collectors.toList())
      );

      // when
      PageResponse<ClothesAttributeDefDto> result = attributeService.findByCondition(condition);

      // then
      assertThat(result.hasNext()).isFalse();
      assertThat(result.nextCursor()).isNull();
      assertThat(result.data()).hasSize(attributes.size());
      verify(attributeRepository).findByCondition(condition);
      verify(attributeRepository).countByKeyword(condition.keywordLike());
    }
  }
}
