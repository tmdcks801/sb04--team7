package com.example.ootd.domain.message.service;

import com.example.ootd.domain.message.dto.DirectMessageDto;
import com.example.ootd.domain.message.dto.MessagePaginationDto;
import com.example.ootd.domain.user.User;
import com.example.ootd.dto.PageResponse;
import java.util.List;
import java.util.UUID;

public interface MessageServiceInterface {

  DirectMessageDto sendMessage(UUID senderId, UUID receiverId, String content);

  PageResponse getMessage(MessagePaginationDto req);
}
