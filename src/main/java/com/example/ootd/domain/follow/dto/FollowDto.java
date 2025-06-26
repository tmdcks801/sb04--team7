package com.example.ootd.domain.follow.dto;

import com.example.ootd.domain.user.dto.UserSummary;
import java.util.UUID;
import lombok.Builder;


@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FollowDto {

@Builder
public record FollowDto(
    UUID id,
    UserSummary followee,
    UserSummary follower
) {

}
