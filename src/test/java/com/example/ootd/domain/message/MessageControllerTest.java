package com.example.ootd.domain.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.ootd.domain.message.controller.MessageController;
import com.example.ootd.domain.message.dto.DirectMessageRequest;
import com.example.ootd.domain.message.dto.MessagePaginationDto;
import com.example.ootd.domain.message.dto.MessagePaginationRequest;
import com.example.ootd.domain.message.service.MessageServiceInterface;
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
class MessageControllerTest {

  @Mock
  private MessageServiceInterface messageService;

  @InjectMocks
  private MessageController messageController;

  @Nested
  class HandleSend {

    @Test
    void handleSend_delegatesToService() {
      UUID sender = UUID.randomUUID();
      UUID receiver = UUID.randomUUID();
      String body = "test";

      DirectMessageRequest req = new DirectMessageRequest(receiver, sender, body);

      messageController.handleSend(req);

      verify(messageService).sendMessage(eq(sender), eq(receiver), eq(body));
    }
  }

  @Nested
  class GetMessage {

    @Test
    void getMessage_returnsPageResponse() {
      UUID ownerId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      UUID cursor = null;
      boolean after = false;
      int limit = 20;

      MessagePaginationRequest req =
          new MessagePaginationRequest(userId, cursor, after, limit);

      MessagePaginationDto expectedDto =
          new MessagePaginationDto(ownerId, userId, cursor, after, limit);

      PageResponse<?> expectedPage = mock(PageResponse.class);
      when(messageService.getMessage(eq(expectedDto))).thenReturn(expectedPage);

      ResponseEntity<PageResponse> res = messageController.getMessage(ownerId, req);

      assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(res.getBody()).isSameAs(expectedPage);
    }
  }
}