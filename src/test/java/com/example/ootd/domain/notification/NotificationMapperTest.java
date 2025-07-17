package com.example.ootd.domain.notification;

import com.example.ootd.domain.notification.mapper.NotificationMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.entity.Notification;
import com.example.ootd.domain.notification.enums.NotificationLevel;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@ExtendWith(MockitoExtension.class)
class NotificationMapperTest {

  private final NotificationMapper mapper = Mappers.getMapper(NotificationMapper.class);

  private static final UUID RECEIVER_ID = UUID.randomUUID();
  private static final Instant FIXED_TIME = Instant.parse("2025-07-16T05:00:00Z");

  @Nested
  class EntityToDtoMapping {

    @Test
    void entityToDto_allFields() {
      Notification entity = Notification.builder()
          .id(UUID.randomUUID())
          .receiverId(RECEIVER_ID)
          .title("aaaaaaaaaaaaaa")
          .content("tttttttttttttttttt")
          .level(NotificationLevel.INFO)
          .createdAt(FIXED_TIME)
          .build();

      NotificationDto dto = mapper.toDto(entity);

      assertThat(dto.id()).isEqualTo(entity.getId());
      assertThat(dto.receiverId()).isEqualTo(RECEIVER_ID);
      assertThat(dto.title()).isEqualTo("aaaaaaaaaaaaaa");
      assertThat(dto.content()).isEqualTo("tttttttttttttttttt");
      assertThat(dto.level()).isEqualTo(NotificationLevel.INFO);
      assertThat(dto.createdAt()).isEqualTo(FIXED_TIME);
    }

    @Test
    void entityList_toDtoList() {
      Notification n1 = Notification.builder()
          .id(UUID.randomUUID())
          .receiverId(RECEIVER_ID)
          .title("111111111111")
          .content("121212")
          .level(NotificationLevel.INFO)
          .createdAt(FIXED_TIME.minusSeconds(30))
          .build();

      Notification n2 = Notification.builder()
          .id(UUID.randomUUID())
          .receiverId(RECEIVER_ID)
          .title("2222222222222")
          .content("333333333333")
          .level(NotificationLevel.WARNING)
          .createdAt(FIXED_TIME)
          .build();

      List<NotificationDto> dtos = mapper.toDtoList(List.of(n1, n2));

      assertThat(dtos).hasSize(2)
          .extracting(NotificationDto::title)
          .containsExactly("111111111111", "2222222222222");
    }
  }

  @Nested
  class DtoToEntityMapping {

    @Test
    void dtoToEntity_allFields() {
      NotificationDto dto = new NotificationDto(
          UUID.randomUUID(),
          FIXED_TIME,
          RECEIVER_ID,
          "121212",
          "232323",
          NotificationLevel.INFO
      );

      Notification entity = mapper.toEntity(dto);

      assertThat(entity.getId()).isEqualTo(dto.id());
      assertThat(entity.getReceiverId()).isEqualTo(RECEIVER_ID);
      assertThat(entity.getTitle()).isEqualTo("121212");
      assertThat(entity.getContent()).isEqualTo("232323");
      assertThat(entity.getLevel()).isEqualTo(NotificationLevel.INFO);
      assertThat(entity.getCreatedAt()).isEqualTo(FIXED_TIME);
    }
  }
}
