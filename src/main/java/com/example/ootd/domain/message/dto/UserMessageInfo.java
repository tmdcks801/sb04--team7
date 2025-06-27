package com.example.ootd.domain.message.dto;

import java.util.UUID;

public record UserMessageInfo(
    UUID userId,
    String name,
    String profileImageUrl
) {

}
