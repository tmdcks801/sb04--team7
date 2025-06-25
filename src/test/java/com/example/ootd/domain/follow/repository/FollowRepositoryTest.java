package com.example.ootd.domain.follow.repository;

import com.example.ootd.domain.follow.entity.Follow;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;


import java.util.List;
import java.util.Optional;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("FollowRepository 테스트")
@ActiveProfiles("test")
class FollowRepositoryTest {


    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    private User follower;
    private User followee;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        follower = User.builder()
                .email("follower@test.com")
                .name("팔로워")
                .build();
        
        followee = User.builder()
                .email("followee@test.com")
                .name("팔로위")
                .build();

        userRepository.save(follower);
        userRepository.save(followee);
    }

    @Nested
    @DisplayName("JPA Repository 테스트")
    class JpaRepositoryTests {

        @Test
        @DisplayName("팔로우 저장 테스트")
        void saveFollow() {
            // given
            Follow follow = Follow.builder()
                .follower(follower)
                .followee(followee)
                .build();

            // when
            Follow savedFollow = followRepository.save(follow);

            // then
            assertThat(savedFollow).isNotNull();
            assertThat(savedFollow.getId()).isNotNull();
            assertThat(savedFollow.getFollower()).isEqualTo(follower);
            assertThat(savedFollow.getFollowee()).isEqualTo(followee);
            assertThat(savedFollow.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("팔로우 단건 조회 테스트")
        void findFollow() {
            // given
            Follow follow = Follow.builder()
                .follower(follower)
                .followee(followee)
                .build();
            Follow savedFollow = followRepository.save(follow);

            // when
            Optional<Follow> foundFollow = followRepository.findById(savedFollow.getId());

            // then
            assertThat(foundFollow).isPresent();
            assertThat(foundFollow.get().getFollower().getId()).isEqualTo(follower.getId());
            assertThat(foundFollow.get().getFollowee().getId()).isEqualTo(followee.getId());
        }

        @Test
        @DisplayName("전체 팔로우 조회 테스트")
        void findAllFollows() {
            // given
            Follow follow1 = Follow.builder()
                .follower(follower)
                .followee(followee)
                .build();

            User testUser = User.builder()
                .email("test@test.com")
                .name("테스트 유저")
                .build();
            userRepository.save(testUser);

            Follow follow2 = Follow.builder()
                .follower(testUser)
                .followee(followee)
                .build();

            followRepository.save(follow1);
            followRepository.save(follow2);

            // when
            List<Follow> follows = followRepository.findAll();

            // then
            assertThat(follows).hasSize(2);
        }

        @Test
        @DisplayName("팔로우 삭제 테스트")
        void deleteFollow() {
            // given
            Follow follow = Follow.builder()
                .follower(follower)
                .followee(followee)
                .build();
            Follow savedFollow = followRepository.save(follow);

            // when
            followRepository.delete(savedFollow);

            // then
            Optional<Follow> deletedFollow = followRepository.findById(savedFollow.getId());
            assertThat(deletedFollow).isEmpty();
        }

        @Test
        @DisplayName("팔로우 수 조회 테스트")
        void countFollows() {
            // given
            Follow follow = Follow.builder()
                .follower(follower)
                .followee(followee)
                .build();
            followRepository.save(follow);

            // when
            long count = followRepository.count();

            // then
            assertThat(count).isEqualTo(1);
        }
    }
}