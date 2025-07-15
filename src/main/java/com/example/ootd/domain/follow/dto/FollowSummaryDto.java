package com.example.ootd.domain.follow.dto;

import java.util.UUID;
import lombok.Builder;


@Builder
public record FollowSummaryDto(
    UUID followeeId,
    long followerCount,
    long followingCount,
    boolean followedByMe,
    UUID followedByMeId,
    boolean followingMe
) {
}
