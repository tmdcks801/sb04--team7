package com.example.ootd.domain.notification;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.ootd.domain.notification.dto.*;
import com.example.ootd.domain.notification.enums.NotificationLevel;
import com.example.ootd.domain.notification.service.impli.NotificationPublisherImpl;
import com.example.ootd.domain.notification.service.inter.NotificationServiceInterface;
import com.example.ootd.domain.user.repository.UserRepository;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class NotificationPublisherImplTest {

  @InjectMocks
  private NotificationPublisherImpl publisher;

  @Mock
  private NotificationServiceInterface notificationService;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Mock
  private UserRepository userRepository;

  @Test
  void publish_createsNotification_andPublishesEvent() {

    NotificationRequest req = new NotificationRequest(
        UUID.randomUUID(), "title", "content", NotificationLevel.INFO);
    NotificationDto dto = mock(NotificationDto.class);

    when(notificationService.createNotification(req)).thenReturn(dto);

    publisher.publish(req);

    verify(notificationService).createNotification(req);
    verify(eventPublisher).publishEvent(dto);
    verifyNoMoreInteractions(notificationService, eventPublisher, userRepository);
  }

  @Test
  void publishToMany_createsBulkNotifications_andPublishesNotificationBulk() {
    // given
    List<UUID> targets = List.of(UUID.randomUUID(), UUID.randomUUID());
    NotificationEvent event = new NotificationEvent(
        "title", "content", NotificationLevel.INFO);

    List<NotificationDto> dtoList = List.of(mock(NotificationDto.class),
        mock(NotificationDto.class));
    when(notificationService.createAll(anyList())).thenReturn(dtoList);

    publisher.publishToMany(event, targets);

    verify(notificationService).createAll(argThat(list -> list.size() == targets.size()));
    verify(eventPublisher).publishEvent(isA(NotificationBulk.class));
  }

  @Test
  void publishToAll_loopsThroughPages_andPublishesInBatches() {

    UUID first = UUID.randomUUID();
    UUID second = UUID.randomUUID();
    List<UUID> firstPage = List.of(first, second);

    when(userRepository.findIdsAfter(nullable(UUID.class), any(Pageable.class)))
        .thenReturn(firstPage)
        .thenReturn(Collections.emptyList());

    when(notificationService.createAll(anyList()))
        .thenReturn(List.of(mock(NotificationDto.class), mock(NotificationDto.class)));

    NotificationEvent event =
        new NotificationEvent("title", "content", NotificationLevel.INFO);

    publisher.publishToAll(event);

    verify(userRepository, atLeastOnce()).findIdsAfter(any(), any(Pageable.class));
    verify(notificationService, atLeastOnce()).createAll(anyList());
    verify(eventPublisher, atLeastOnce()).publishEvent(isA(NotificationBulk.class));
  }
}