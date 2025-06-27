package com.example.ootd.domain.message.service;

import com.example.ootd.domain.message.dto.DirectMessageDto;
import com.example.ootd.domain.user.User;
import java.util.List;
import java.util.UUID;

public interface MessageServiceInterface {

  DirectMessageDto sendMessage(UUID senderId, UUID receiverId, String content);

  List<DirectMessageDto> getMessage(UUID sender, UUID receiver, UUID cursor, boolean isAfter,
      int limit);
}
