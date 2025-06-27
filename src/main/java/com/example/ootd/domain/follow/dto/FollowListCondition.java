package com.example.ootd.domain.follow.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public record FollowListCondition(
    String cursor,
    UUID idAfter,
    int limit,
    String nameLike,
    String orderBy,
    Direction direction

) {
  public String orderBy() {
    return orderBy == null ? "createdAt" : orderBy;
  }

  public Direction direction() {
    return direction == null ? Direction.ASCENDING : direction;
  }
}
