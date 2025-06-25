package com.example.ootd.domain.follow.service.impl;


import com.example.ootd.domain.follow.dto.FollowCreateRequest;
import com.example.ootd.domain.follow.dto.FollowDto;
import com.example.ootd.domain.follow.dto.FollowSummaryDto;
import com.example.ootd.domain.follow.entity.Follow;
import com.example.ootd.domain.follow.mapper.FollowMapper;
import com.example.ootd.domain.follow.repository.FollowRepository;
import com.example.ootd.domain.follow.service.FollowService;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.repository.UserRepository;
import java.util.UUID;
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
   * @return FollowDto
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

  /**
   * 팔로우 요약 정보 조회
   * @param userId
   * @return FollowSummaryDto
   */
  @Override
  public FollowSummaryDto getSummaryFollow(UUID userId) {
    log.info("팔로우 요약 조회 : userId: {}", userId);

    // 유저 조회
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

    // 팔로우 관계 요약 생성
    FollowSummaryDto summary = FollowSummaryDto.builder()
        .followeeId(user.getId())
        .followerCount(followRepository.countByFolloweeId(user.getId()))
        .followingCount(followRepository.countByFollowerId(user.getId()))
        .followedByMe(followRepository.existsByFollowerIdAndFolloweeId(userId, user.getId()))
        .followedByMeId(userId)
        .followingByMe(followRepository.existsByFollowerIdAndFolloweeId(user.getId(), userId))
        .build();

    return summary;
  }

  /**
   * 팔로우 삭제
   * @param followId
   */
  @Override
  @Transactional
  public void deleteFollow(UUID followId) {
    log.info("팔로우 삭제 : followId: {}", followId);

    // 팔로우 조회
    Follow follow = followRepository.findById(followId)
        .orElseThrow(() -> new IllegalArgumentException("팔로우를 찾을 수 없습니다."));

    // 팔로우 삭제
    followRepository.delete(follow);
    log.info("팔로우 삭제 완료 : followId: {}", followId);
  }
}
