package com.example.ootd.domain.user.dto;

import java.util.List;
import java.util.UUID;

public record UserPagedResponse(
    List<UserDto> data,
    String nextCursor,
    UUID nextIdAfter,
    boolean hasNext,
    Long totalCount,
    String sortBy,
    String sortDirection
) {
}
