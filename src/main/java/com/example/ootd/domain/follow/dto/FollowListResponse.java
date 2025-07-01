package com.example.ootd.domain.follow.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record FollowListResponse(
    List<FollowDto> data,
    String nextCursor,
    UUID nextIdAfter,
    boolean hasNext,
    int totalCount,
    String sortBy,
    Direction sortDirection
) {

}
