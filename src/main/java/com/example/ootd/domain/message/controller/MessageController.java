package com.example.ootd.domain.message.controller;

import com.example.ootd.domain.message.dto.DirectMessageDto;
import com.example.ootd.domain.message.dto.DirectMessageRequest;
import com.example.ootd.domain.message.service.MessageServiceInterface;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequiredArgsConstructor
public class MessageController {

  private final MessageServiceInterface messageService;

//  @MessageMapping("/direct-message/send")
//  public void handleSend(@Payload DirectMessageRequest req) {
//
//    messageService.sendMessage(req.senderId(), req.receiverId(), req.content());
//  }

}
