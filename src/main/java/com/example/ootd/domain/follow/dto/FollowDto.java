package com.example.ootd.domain.follow.dto;

import com.example.ootd.domain.user.dto.UserSummary;
import java.util.UUID;
import lombok.Builder;


@Builder
public record FollowDto(
    UUID id,
    UserSummary followee,
    UserSummary follower
) {

}
