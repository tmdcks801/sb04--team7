package com.example.ootd.domain.message.controller;

import com.example.ootd.config.api.MessageApi;
import com.example.ootd.domain.message.dto.DirectMessageRequest;
import com.example.ootd.domain.message.dto.MessagePaginationDto;
import com.example.ootd.domain.message.dto.MessagePaginationRequest;
import com.example.ootd.domain.message.service.MessageServiceInterface;
import com.example.ootd.dto.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/direct-messages")
public class MessageController implements MessageApi {

  private final MessageServiceInterface messageService;

  @MessageMapping("/direct-messages_send")   // 클라이언트 가 /pub/direct-messages_send
  public void handleSend(@Valid @Payload DirectMessageRequest req) {
    messageService.sendMessage(req.senderId(), req.receiverId(), req.content());
  }

  @GetMapping
  public ResponseEntity<PageResponse> getMessage(
      @AuthenticationPrincipal(expression = "user.id") UUID ownerId,
      @Valid @ModelAttribute MessagePaginationRequest request) {

    MessagePaginationDto messagePaginationDto = new MessagePaginationDto//아이디 넣어야해서 냅둠
        (ownerId, request.userId(), request.cursor(),
            request.isAfter(), request.limit());
    return ResponseEntity.ok(messageService.getMessage(messagePaginationDto));
  }


}
