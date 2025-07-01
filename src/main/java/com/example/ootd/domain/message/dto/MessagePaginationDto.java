package com.example.ootd.domain.message.dto;

import java.util.UUID;

public record MessagePaginationDto(
    UUID sender,
    UUID receiver,
    UUID cursor,
    boolean isAfter,
    int limit) {

}
