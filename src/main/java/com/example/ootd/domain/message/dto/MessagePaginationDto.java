package com.example.ootd.domain.message.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;
import org.springframework.lang.Nullable;

public record MessagePaginationDto(
    @NotNull UUID sender,
    @NotNull UUID receiver,
    UUID cursor,
    boolean isAfter,
    @Positive int limit) {

}
