package com.example.ootd.domain.follow.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public record FollowListCondition(
    String cursor,
    UUID idAfter,
    int limit,
    String nameLike
) {

}
