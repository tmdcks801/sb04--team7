package com.example.ootd.domain.follow.service;

import com.example.ootd.domain.follow.dto.FollowCreateRequest;
import com.example.ootd.domain.follow.dto.FollowDto;
import com.example.ootd.domain.follow.dto.FollowSummaryDto;
import java.util.UUID;

public interface FollowService {

    /**
     * @param request follower and followee IDs
     */
  FollowDto createFollow(FollowCreateRequest request);

  FollowSummaryDto getSummaryFollow(UUID userId);

  void deleteFollow(UUID followId);
}
