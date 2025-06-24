package com.example.ootd.domain.message.service;

import com.example.ootd.domain.message.dto.DirectMessageDto;
import java.util.UUID;

public interface MessageServiceInterface {

  DirectMessageDto sendMessage(User sender, User receiver, String content);

  void getMessage(UUID id, String cursorr, UUID isAfter, int limit);
}
