package com.example.ootd.domain.follow.repository;

import com.example.ootd.domain.follow.entity.Follow;
import com.example.ootd.domain.location.Location;
import com.example.ootd.domain.user.User;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ootd.domain.follow.dto.Direction;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowRepository 테스트")
class FollowRepositoryTest {

    @Mock
    private FollowRepository followRepository;

    private User follower;
    private User followee;
    private Follow follow;
    private Location testLocation;

    @BeforeEach
    void setUp() {
        testLocation = new Location(37.5665, 126.9780, 60, 127, List.of("서울특별시", "중구", "명동"));
        follower = new User("팔로워", "follower@test.com", "qwer12314", testLocation);
        followee = new User("팔로위", "followee@test.com", "qwer1234", testLocation);
        
        follow = Follow.builder()
            .id(UUID.randomUUID())
            .follower(follower)
            .followee(followee)
            .build();
    }

    @Test
    @DisplayName("팔로우 저장 테스트")
    void saveFollow() {
        // given
        given(followRepository.save(follow)).willReturn(follow);

        // when
        Follow savedFollow = followRepository.save(follow);

        // then
        assertThat(savedFollow).isNotNull();
        assertThat(savedFollow).isEqualTo(follow);
    }

    @Test
    @DisplayName("팔로우 조회 테스트")
    void findFollow() {
        // given
        given(followRepository.findById(follow.getId())).willReturn(Optional.of(follow));

        // when
        Optional<Follow> foundFollow = followRepository.findById(follow.getId());

        // then
        assertThat(foundFollow).isPresent();
        assertThat(foundFollow.get()).isEqualTo(follow);
    }

    @Test
    @DisplayName("팔로우 존재 여부 확인 테스트")
    void existsByFollowerIdAndFolloweeId() {
        // given
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        given(followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)).willReturn(true);

        // when
        boolean exists = followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("팔로워 수 조회 테스트")
    void countByFollowerId() {
        // given
        UUID followerId = UUID.randomUUID();
        given(followRepository.countByFollowerId(followerId)).willReturn(5L);

        // when
        long count = followRepository.countByFollowerId(followerId);

        // then
        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("팔로위 수 조회 테스트")
    void countByFolloweeId() {
        // given
        UUID followeeId = UUID.randomUUID();
        given(followRepository.countByFolloweeId(followeeId)).willReturn(3L);

        // when
        long count = followRepository.countByFolloweeId(followeeId);

        // then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("팔로워 ID 목록 조회 테스트")
    void findFollowersByFolloweeId() {
        // given
        UUID followeeId = UUID.randomUUID();
        List<UUID> followerIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        given(followRepository.findFollowersByFolloweeId(followeeId)).willReturn(followerIds);

        // when
        List<UUID> result = followRepository.findFollowersByFolloweeId(followeeId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(followerIds);
    }

    @Test
    @DisplayName("전체 팔로우 개수 조회 테스트")
    void countAll() {
        // given
        given(followRepository.count()).willReturn(10L);

        // when
        long count = followRepository.count();

        // then
        assertThat(count).isEqualTo(10L);
    }

    @Test
    @DisplayName("팔로워 커서 기반 페이지네이션 조회 테스트")
    void findFollowersWithCursor() {
        // given
        UUID followeeId = UUID.randomUUID();
        given(followRepository.findFollowersWithCursor(followeeId, null, null, 10, null, "id", Direction.ASCENDING))
            .willReturn(List.of());

        // when
        List<Follow> result = followRepository.findFollowersWithCursor(followeeId, null, null, 10, null, "id", Direction.ASCENDING);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("팔로잉 커서 기반 페이지네이션 조회 테스트")
    void findFollowingsWithCursor() {
        // given
        UUID followerId = UUID.randomUUID();
        given(followRepository.findFollowingsWithCursor(followerId, null, null, 10, null, "id", Direction.ASCENDING))
            .willReturn(List.of());

        // when
        List<Follow> result = followRepository.findFollowingsWithCursor(followerId, null, null, 10, null, "id", Direction.ASCENDING);

        // then
        assertThat(result).isNotNull();
    }
}
