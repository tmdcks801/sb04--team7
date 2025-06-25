package com.example.ootd.domain.follow.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
public record FollowCreateRequest(
  @NotNull UUID followerId,
  @NotNull UUID followeeId
) {
}
