package com.example.ootd.domain.follow.service;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.example.ootd.domain.follow.dto.FollowCreateRequest;
import com.example.ootd.domain.follow.dto.FollowDto;
import com.example.ootd.domain.follow.dto.FollowSummaryDto;
import com.example.ootd.domain.follow.entity.Follow;
import com.example.ootd.domain.follow.mapper.FollowMapper;
import com.example.ootd.domain.follow.repository.FollowRepository;
import com.example.ootd.domain.follow.service.impl.FollowServiceImpl;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.dto.UserSummary;
import com.example.ootd.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowService 테스트")
public class FollowServiceImplTest {

    @Mock
    private FollowRepository followRepository;
    
    @Mock
    private FollowMapper followMapper;
    
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FollowServiceImpl followService;

    private User follower;
    private User followee;
    private Follow follow;
    private FollowDto followDto;
    private FollowCreateRequest followCreateRequest;
    private FollowSummaryDto followSummaryDto;
    private UUID followerId;
    private UUID followeeId;
    private UUID followId;

    @BeforeEach
    void setUp() {
        // UUID 생성
        followerId = UUID.randomUUID();
        followeeId = UUID.randomUUID();
        followId = UUID.randomUUID();

        // User 엔티티 생성
        follower = User.builder()
                .id(followerId)
                .name("팔로워")
                .email("follower@test.com")
                .build();

        followee = User.builder()
                .id(followeeId)
                .name("팔로위")
                .email("followee@test.com")
                .build();

        // Follow 엔티티 생성
        follow = Follow.builder()
                .id(followId)
                .follower(follower)
                .followee(followee)
                .build();

        // UserSummary 생성
        UserSummary followerSummary = new UserSummary(followerId, "팔로워", null);
        UserSummary followeeSummary = new UserSummary(followeeId, "팔로위", null);

        // DTO 생성
        followDto = FollowDto.builder()
                .id(followId)
                .follower(followerSummary)
                .followee(followeeSummary)
                .build();

        followCreateRequest = new FollowCreateRequest(followerId, followeeId);

        followSummaryDto = FollowSummaryDto.builder()
                .followeeId(followeeId)
                .followerCount(1L)
                .followingCount(1L)
                .followedByMe(false)
                .followedByMeId(followerId)
                .followingByMe(false)
                .build();
    }

    @Nested
    @DisplayName("팔로우 생성 테스트")
    class CreateFollowTests {

        @Test
        @DisplayName("팔로우 생성 성공")
        void createFollowSuccess() {
            // given
            given(userRepository.findById(followerId)).willReturn(Optional.of(follower));
            given(userRepository.findById(followeeId)).willReturn(Optional.of(followee));
            given(followRepository.save(any(Follow.class))).willReturn(follow);
            given(followMapper.toDto(any(Follow.class))).willReturn(followDto);

            // when
            FollowDto result = followService.createFollow(followCreateRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.followee().userId()).isEqualTo(followeeId);
            assertThat(result.follower().userId()).isEqualTo(followerId);
        }

        @Test
        @DisplayName("팔로우 생성 실패 - 이미 팔로우 중")
        void createFollowAlready() {
            // given
            given(followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)).willReturn(true);

            // then
            assertThrows(IllegalArgumentException.class, () -> followService.createFollow(followCreateRequest));
        }

        @Test
        @DisplayName("팔로우 생성 실패 - 팔로워를 찾을 수 없음")
        void createFollow_FollowerNotFound() {
            // given
            given(userRepository.findById(followerId)).willReturn(Optional.empty());

            // then
            assertThrows(IllegalArgumentException.class, () -> followService.createFollow(followCreateRequest));
        }

        @Test
        @DisplayName("팔로우 생성 실패 - 팔로우 대상 사용자를 찾을 수 없음")
        void createFollow_FolloweeNotFound() {
            // given
            given(userRepository.findById(followerId)).willReturn(Optional.of(follower));
            given(userRepository.findById(followeeId)).willReturn(Optional.empty());

            // then
            assertThrows(IllegalArgumentException.class, () -> followService.createFollow(followCreateRequest));
        }
    }

    @Nested
    @DisplayName("팔로우 요약 조회 테스트")
    class GetSummaryFollowTests {

        @Test
        @DisplayName("팔로우 요약 조회 성공")
        void getSummaryFollowSuccess() {
            // given
            given(userRepository.findById(followerId)).willReturn(Optional.of(follower));
            given(followRepository.countByFolloweeId(followerId)).willReturn(1L);
            given(followRepository.countByFollowerId(followerId)).willReturn(1L);
            given(followRepository.existsByFollowerIdAndFolloweeId(followerId, followerId)).willReturn(false);

            // then
            FollowSummaryDto result = followService.getSummaryFollow(followerId);

            // when
            assertThat(result.followerCount()).isEqualTo(1L);
            assertThat(result.followingCount()).isEqualTo(1L);
            assertThat(result.followedByMe()).isFalse();
        }

        @Test
        @DisplayName("팔로우 요약 조회 실패 - 유저를 찾을 수 없음")
        void getSummaryFollow_UserNotFound() {
            // given
            given(userRepository.findById(followerId)).willReturn(Optional.empty());

            // then
            assertThrows(IllegalArgumentException.class, () -> followService.getSummaryFollow(followerId));
        }
    }
}
