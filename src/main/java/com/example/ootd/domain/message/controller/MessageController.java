package com.example.ootd.domain.message.controller;

import com.example.ootd.domain.message.dto.DirectMessageRequest;
import com.example.ootd.domain.message.service.MessageServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

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
