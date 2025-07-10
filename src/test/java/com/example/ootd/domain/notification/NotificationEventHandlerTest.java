package com.example.ootd.domain.notification;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.ootd.domain.notification.dto.NotificationBulk;
import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.handler.NotificationEventHandler;
import com.example.ootd.domain.sse.service.SsePushServiceInterface;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationEventHandlerTest {

  @Mock
  private SsePushServiceInterface pushService;

  private NotificationEventHandler handler;

  @BeforeEach
  void setUp() {
    handler = new NotificationEventHandler(pushService);
  }

  @Test
  void handle_single_success() {
    NotificationDto dto = mock(NotificationDto.class);

    handler.handle(dto);

    verify(pushService).push(dto);
    verifyNoMoreInteractions(pushService);
  }

  @Test
  void handle_single_exception_is_caught() {
    NotificationDto dto = mock(NotificationDto.class);
    doThrow(new RuntimeException("push fail")).when(pushService).push(dto);

    assertDoesNotThrow(() -> handler.handle(dto));
    verify(pushService).push(dto);
  }

  @Test
  void handle_bulk_success() {
    NotificationDto dto1 = mock(NotificationDto.class);
    NotificationDto dto2 = mock(NotificationDto.class);
    NotificationBulk bulk = mock(NotificationBulk.class);
    when(bulk.notificationDtoList()).thenReturn(List.of(dto1, dto2));

    handler.handle(bulk);

    verify(pushService, times(2)).push(any(NotificationDto.class));
  }

  @Test
  void handle_bulk_partial_failure_continues() {
    NotificationDto failDto = mock(NotificationDto.class);
    NotificationDto okDto = mock(NotificationDto.class);
    NotificationBulk bulk = mock(NotificationBulk.class);
    when(bulk.notificationDtoList()).thenReturn(List.of(failDto, okDto));

    doThrow(new RuntimeException("fail")).when(pushService).push(failDto);

    handler.handle(bulk);

    verify(pushService).push(failDto);
    verify(pushService).push(okDto);
  }
}