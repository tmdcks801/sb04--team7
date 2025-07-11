package com.example.ootd.domain.follow.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.ootd.domain.follow.dto.FollowCreateRequest;
import com.example.ootd.domain.follow.dto.FollowDto;
import com.example.ootd.domain.follow.dto.FollowSummaryDto;
import com.example.ootd.domain.follow.entity.Follow;
import com.example.ootd.domain.follow.mapper.FollowMapper;
import com.example.ootd.domain.follow.repository.FollowRepository;
import com.example.ootd.domain.follow.service.impl.FollowServiceImpl;
import com.example.ootd.domain.location.Location;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.dto.UserSummary;
import com.example.ootd.domain.notification.service.inter.NotificationPublisherInterface;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.exception.OotdException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @Mock
    private NotificationPublisherInterface notificationPublisher;

    @InjectMocks
    private FollowServiceImpl followService;

    private User follower;
    private User followee;
    private Follow follow;
    private FollowDto followDto;
    private FollowCreateRequest followCreateRequest;
    private UUID followerId;
    private UUID followeeId;
    private UUID followId;

    @BeforeEach
    void setUp() {
        followerId = UUID.randomUUID();
        followeeId = UUID.randomUUID();
        followId = UUID.randomUUID();

        Location testLocation = new Location(37.5665, 126.9780, 60, 127, List.of("서울특별시", "중구", "명동"));
        this.follower = new User("팔로워", "follower@test.com", "qwer1234", testLocation);
        this.followee = new User("팔로위", "followee@test.com", "qwer1234", testLocation);

        follow = Follow.builder()
                .id(followId)
                .follower(this.follower)
                .followee(this.followee)
                .build();

        UserSummary followerSummary = new UserSummary(followerId, "팔로워", null);
        UserSummary followeeSummary = new UserSummary(followeeId, "팔로위", null);

        followDto = FollowDto.builder()
                .id(followId)
                .follower(followerSummary)
                .followee(followeeSummary)
                .build();

        followCreateRequest = new FollowCreateRequest(followerId, followeeId);
    }

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
        assertThrows(OotdException.class, () -> followService.createFollow(followCreateRequest));
    }

/*    @Test
    @DisplayName("팔로우 요약 조회 성공")
    void getSummaryFollowSuccess() {
        // given
        long followerCount = 5L;
        long followingCount = 10L;
        boolean isFollowing = false;

        given(userRepository.findById(followeeId)).willReturn(Optional.of(followee));
        given(followRepository.countByFolloweeId(followeeId)).willReturn(followerCount);
        given(followRepository.countByFollowerId(followeeId)).willReturn(followingCount);
        given(followRepository.existsByFollowerIdAndFolloweeId(follower.getId(), followee.getId())).willReturn(isFollowing);

        // when
        FollowSummaryDto result = followService.getSummaryFollow(followeeId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.followerCount()).isEqualTo(followerCount);
        assertThat(result.followingCount()).isEqualTo(followingCount);
        assertThat(result.followingByMe()).isEqualTo(isFollowing);
    }*/

    @Test
    @DisplayName("팔로우 삭제 성공")
    void deleteFollowSuccess() {
        // given
        given(followRepository.findById(followId)).willReturn(Optional.of(follow));

        // when
        followService.deleteFollow(followId);

        // then
        verify(followRepository).delete(follow);
    }

    @Test
    @DisplayName("팔로우 삭제 실패 - 팔로우를 찾을 수 없음")
    void deleteFollowNotFound() {
        // given
        given(followRepository.findById(followId)).willReturn(Optional.empty());

        // then
        assertThrows(OotdException.class, () -> followService.deleteFollow(followId));
    }
}
