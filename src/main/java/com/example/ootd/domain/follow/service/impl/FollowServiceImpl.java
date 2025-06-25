package com.example.ootd.domain.follow.service.impl;


import com.example.ootd.domain.follow.dto.FollowCreateRequest;
import com.example.ootd.domain.follow.dto.FollowDto;
import com.example.ootd.domain.follow.entity.Follow;
import com.example.ootd.domain.follow.mapper.FollowMapper;
import com.example.ootd.domain.follow.repository.FollowRepository;
import com.example.ootd.domain.follow.service.FollowService;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowServiceImpl implements FollowService {

  private final FollowRepository followRepository;
  private final UserRepository userRepository;
  private final FollowMapper followMapper;

  /**
   * 팔로우 생성
   * @param request follower and followee IDs
   * @return
   */
  @Override
  @Transactional
  public FollowDto createFollow(FollowCreateRequest request) {
    log.info("팔로워 생성 : followerId: {}, followeeId: {}", request.followerId(), request.followeeId());

    // 이미 팔로우 하는지 확인
    if (followRepository.existsByFollowerIdAndFolloweeId(request.followerId(), request.followeeId())) {
      throw new IllegalArgumentException("이미 팔로우 중인 사용자입니다.");
    }

    // 팔로워와 팔로우 대상 사용자를 조회
    User follower = userRepository.findById(request.followerId())
        .orElseThrow(() -> new IllegalArgumentException("팔로워를 찾을 수 없습니다."));

    User followee = userRepository.findById(request.followeeId())
        .orElseThrow(() -> new IllegalArgumentException("팔로우 대상 사용자를 찾을 수 없습니다."));


    // 팔로우 관계 생성
    Follow follow = Follow.builder()
        .follower(follower)
        .followee(followee)
        .build();

    Follow savedFollow = followRepository.save(follow);

    return followMapper.toDto(savedFollow);
  }

}
