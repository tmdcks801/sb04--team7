package com.example.ootd.domain.notification;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.dto.NotificationRequest;
import com.example.ootd.domain.notification.entity.Notification;
import com.example.ootd.domain.notification.enums.NotificationLevel;
import com.example.ootd.domain.notification.mapper.NotificationMapper;
import com.example.ootd.domain.notification.repository.NotificationRepository;
import com.example.ootd.domain.notification.service.impli.NotificationServiceImpl;
import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.notification.*;
import com.mongodb.bulk.BulkWriteResult;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.*;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

  @Mock
  NotificationRepository repository;
  @Mock
  MongoTemplate mongoTemplate;
  @Mock
  NotificationMapper mapper;
  @Mock
  BulkOperations bulkOps;
  @Mock
  BulkWriteResult bulkResult;
  @InjectMocks
  NotificationServiceImpl service;

  @Test
  void createNotificationDto_success() {
    NotificationDto inDto = mock(NotificationDto.class);
    Notification entity = mock(Notification.class);
    NotificationDto outDto = mock(NotificationDto.class);

    try (MockedStatic<Notification> stat = mockStatic(Notification.class)) {
      stat.when(() -> Notification.createNotification(inDto)).thenReturn(entity);
      when(repository.save(entity)).thenReturn(entity);
      when(mapper.toDto(entity)).thenReturn(outDto);

      assertThat(service.createNotification(inDto)).isSameAs(outDto);
      verify(repository).save(entity);
    }
  }

  @Test
  void createNotificationDto_fail_onSave() {
    NotificationDto dto = mock(NotificationDto.class);
    Notification entity = mock(Notification.class);

    try (MockedStatic<Notification> stat = mockStatic(Notification.class)) {
      stat.when(() -> Notification.createNotification(dto)).thenReturn(entity);
      when(repository.save(entity)).thenThrow(new RuntimeException("mongo down"));

      assertThatThrownBy(() -> service.createNotification(dto))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Test
  void createNotificationReq_success() {
    NotificationRequest req = mock(NotificationRequest.class);
    Notification ent = mock(Notification.class);
    NotificationDto dto = mock(NotificationDto.class);

    try (MockedStatic<Notification> stat = mockStatic(Notification.class)) {
      stat.when(() -> Notification.createNotification(req)).thenReturn(ent);
      when(repository.save(ent)).thenReturn(ent);
      when(mapper.toDto(ent)).thenReturn(dto);

      assertThat(service.createNotification(req)).isSameAs(dto);
    }
  }

  @Test
  void createNotificationReq_fail_customException() {
    NotificationRequest req = mock(NotificationRequest.class);
    Notification ent = mock(Notification.class);

    try (MockedStatic<Notification> stat = mockStatic(Notification.class)) {
      stat.when(() -> Notification.createNotification(req)).thenReturn(ent);

      when(repository.save(ent))
          .thenThrow(new NotificationCreateError(ErrorCode.FAIL_CREATE_NOTIFICATION));

      assertThatThrownBy(() -> service.createNotification(req))
          .isInstanceOf(NotificationCreateError.class);
    }
  }

  @Test
  void get_success() {
    UUID id = UUID.randomUUID();
    Notification ent = mock(Notification.class);
    NotificationDto dto = mock(NotificationDto.class);

    when(repository.findById(id)).thenReturn(Optional.of(ent));
    when(mapper.toDto(ent)).thenReturn(dto);

    assertThat(service.get(id)).isSameAs(dto);
  }

  @Test
  void get_notFound() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.get(id))
        .isInstanceOf(IllegalArgumentException.class);
  }


  @Test
  void readNotification_success() {
    UUID id = UUID.randomUUID();
    Notification ent = mock(Notification.class);

    when(repository.findById(id)).thenReturn(Optional.of(ent));

    service.readNotification(id);

    verify(repository).delete(ent);
  }

  @Test
  void readNotification_notFound() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.readNotification(id))
        .isInstanceOf(NotFoundNotification.class);
  }

  @Test
  void createAll_success() {
    NotificationRequest req1 = mock(NotificationRequest.class);
    NotificationRequest req2 = mock(NotificationRequest.class);
    List<NotificationRequest> reqs = List.of(req1, req2);

    when(req1.level()).thenReturn(NotificationLevel.INFO);
    when(req2.level()).thenReturn(NotificationLevel.INFO);

    when(mongoTemplate.bulkOps(BulkMode.UNORDERED, Notification.class)).thenReturn(bulkOps);
    when(bulkOps.insert(any(Notification.class))).thenReturn(bulkOps);
    when(bulkOps.execute()).thenReturn(bulkResult);
    when(mapper.toDto(any(Notification.class)))
        .thenReturn(mock(NotificationDto.class));

    List<NotificationDto> result = service.createAll(reqs);

    assertThat(result).hasSize(2);
    verify(bulkOps, times(1)).execute();
  }

}
