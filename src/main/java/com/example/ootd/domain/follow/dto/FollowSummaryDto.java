package com.example.ootd.domain.follow.dto;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FollowSummaryDto {

  private UUID followerId;
  private long followerCount;
  private long followingCount;
  private boolean followByMe;
  private UUID followedByMe;
  private boolean followingByMe;

}
