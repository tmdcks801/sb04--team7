package com.example.ootd.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.ootd.domain.notification.controller.NotificationController;
import com.example.ootd.domain.notification.service.inter.NotificationServiceInterface;
import com.example.ootd.dto.PageResponse;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

  @Mock
  private NotificationServiceInterface notificationService;

  @InjectMocks
  private NotificationController notificationController;

  @Nested
  class GetNotifications {

    @Test
    void getNotifications_returnsPageResponse() {

      UUID receiverId = UUID.randomUUID();
      String cursor = null;
      int limit = 20;

      PageResponse<?> expectedPage = mock(PageResponse.class);
      when(notificationService.getPageNation(eq(receiverId), eq(cursor), eq(limit)))
          .thenReturn(expectedPage);

      ResponseEntity<PageResponse> res =
          notificationController.getNotifications(receiverId, cursor, limit);

      // then
      assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(res.getBody()).isSameAs(expectedPage);
    }
  }

  @Nested
  class ReadNotification {

    @Test
    void readNotification_returnsNoContent() {

      UUID notificationId = UUID.randomUUID();

      ResponseEntity<Void> res =
          notificationController.readNotification(notificationId.toString());

      assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      verify(notificationService).readNotification(eq(notificationId));
    }
  }
}