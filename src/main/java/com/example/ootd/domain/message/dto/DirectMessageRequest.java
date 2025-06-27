package com.example.ootd.domain.message.dto;

import java.util.UUID;

public record DirectMessageRequest(
    UUID receiverId, UUID senderId, String content
) {

}
