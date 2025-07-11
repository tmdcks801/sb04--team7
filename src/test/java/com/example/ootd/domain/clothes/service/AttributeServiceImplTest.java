package com.example.ootd.domain.clothes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.ootd.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.example.ootd.domain.clothes.entity.Attribute;
import com.example.ootd.domain.clothes.mapper.AttributeMapper;
import com.example.ootd.domain.clothes.repository.AttributeRepository;
import com.example.ootd.domain.clothes.service.impl.AttributeServiceImpl;
import com.example.ootd.domain.notification.dto.NotificationEvent;
import com.example.ootd.domain.notification.service.inter.NotificationPublisherInterface;
import com.example.ootd.exception.clothes.AttributeNameAlreadyExistsException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

  @Nested
  @DisplayName("create() - 의상 속성 정의 등록")
  class createTest {

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
    void create_failed_duplicate() {

      // given
      ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest(
          "테스트", List.of("test1", "test2")
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
}
