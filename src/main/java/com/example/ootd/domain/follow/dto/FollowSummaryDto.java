package com.example.ootd.domain.follow.dto;

import java.util.UUID;
import lombok.Builder;


@Builder
public record FollowSummaryDto(
    UUID followerId,
    long followingCount,
    long followerCount,
    boolean followByMe,
    UUID followedByMe,
    boolean followingByMe
) {
}
