package com.example.ootd.domain.message.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DirectMessageDto(

    UUID id,
    LocalDateTime createdAt,
    UserMessageInfo sender,
    UserMessageInfo receiver,
    String content
) {


}
