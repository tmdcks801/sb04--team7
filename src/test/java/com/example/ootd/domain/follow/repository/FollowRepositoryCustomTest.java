package com.example.ootd.domain.follow.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.example.ootd.domain.follow.dto.Direction;
import com.example.ootd.domain.follow.entity.Follow;
import com.example.ootd.domain.follow.repository.custom.impl.FollowRepositoryCustomImpl;
import com.example.ootd.domain.user.User;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowRepositoryCustomImpl 테스트")
class FollowRepositoryCustomTest {

  @Mock
  private JPAQueryFactory queryFactory;

  @Mock
  private JPAQuery<Follow> jpaQuery;

  private FollowRepositoryCustomImpl followRepositoryCustomImpl;

  private UUID followerId;
  private UUID followeeId;
  private Follow follow;

  @BeforeEach
  void setUp() {
    followRepositoryCustomImpl = new FollowRepositoryCustomImpl(queryFactory);

    followerId = UUID.randomUUID();
    followeeId = UUID.randomUUID();

    User follower = mock(User.class);
    User followee = mock(User.class);

    follow = mock(Follow.class);
    lenient().when(follow.getFollower()).thenReturn(follower);
    lenient().when(follow.getFollowee()).thenReturn(followee);

    given(queryFactory.selectFrom(any(EntityPath.class))).willReturn(jpaQuery);
    given(jpaQuery.leftJoin(any(EntityPath.class))).willReturn(jpaQuery);
    given(jpaQuery.fetchJoin()).willReturn(jpaQuery);
    given(jpaQuery.where(any(Predicate.class))).willReturn(jpaQuery);
    given(jpaQuery.orderBy(any(OrderSpecifier.class))).willReturn(jpaQuery);
    given(jpaQuery.limit(anyLong())).willReturn(jpaQuery);
  }

  @Test
  @DisplayName("팔로잉 목록 조회 - 기본 조건")
  void findFollowingsWithCursor_basicCondition() {
    // given
    given(jpaQuery.fetch()).willReturn(List.of(follow));

    // when
    List<Follow> result = followRepositoryCustomImpl.findFollowingsWithCursor(
        followerId, null, null, 10, null, "id", Direction.ASCENDING);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(follow);
  }

  @Test
  @DisplayName("팔로잉 목록 조회 - 커서 조건")
  void findFollowingsWithCursor_withCursor() {
    // given
    given(jpaQuery.fetch()).willReturn(List.of(follow));

    // when
    List<Follow> result = followRepositoryCustomImpl.findFollowingsWithCursor(
        followerId, "cursor", followeeId, 10, null, "id", Direction.ASCENDING);

    // then
    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("팔로잉 목록 조회 - 이름 검색")
  void findFollowingsWithCursor_withNameLike() {
    // given
    given(jpaQuery.fetch()).willReturn(List.of(follow));

    // when
    List<Follow> result = followRepositoryCustomImpl.findFollowingsWithCursor(
        followerId, null, null, 10, "test", "name", Direction.DESCENDING);

    // then
    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("팔로잉 목록 조회 - createdAt 정렬")
  void findFollowingsWithCursor_orderByCreatedAt() {
    // given
    given(jpaQuery.fetch()).willReturn(List.of(follow));

    // when
    List<Follow> result = followRepositoryCustomImpl.findFollowingsWithCursor(
        followerId, null, null, 10, null, "createdAt", Direction.ASCENDING);

    // then
    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("팔로잉 목록 조회 - 알 수 없는 정렬 기준")
  void findFollowingsWithCursor_unknownOrderBy() {
    // given
    given(jpaQuery.fetch()).willReturn(List.of(follow));

    // when
    List<Follow> result = followRepositoryCustomImpl.findFollowingsWithCursor(
        followerId, null, null, 10, null, "unknown", Direction.ASCENDING);

    // then
    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("팔로잉 목록 조회 - 빈 결과")
  void findFollowingsWithCursor_emptyResult() {
    // given
    given(jpaQuery.fetch()).willReturn(List.of());

    // when
    List<Follow> result = followRepositoryCustomImpl.findFollowingsWithCursor(
        followerId, null, null, 10, null, "id", Direction.ASCENDING);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("팔로워 목록 조회 - 기본 조건")
  void findFollowersWithCursor_basicCondition() {
    // given
    given(jpaQuery.fetch()).willReturn(List.of(follow));

    // when
    List<Follow> result = followRepositoryCustomImpl.findFollowersWithCursor(
        followeeId, null, null, 10, null, "id", Direction.ASCENDING);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(follow);
  }

  @Test
  @DisplayName("팔로워 목록 조회 - 커서 조건")
  void findFollowersWithCursor_withCursor() {
    // given
    given(jpaQuery.fetch()).willReturn(List.of(follow));

    // when
    List<Follow> result = followRepositoryCustomImpl.findFollowersWithCursor(
        followeeId, "cursor", followerId, 10, null, "id", Direction.ASCENDING);

    // then
    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("팔로워 목록 조회 - 이름 검색")
  void findFollowersWithCursor_withNameLike() {
    // given
    given(jpaQuery.fetch()).willReturn(List.of(follow));

    // when
    List<Follow> result = followRepositoryCustomImpl.findFollowersWithCursor(
        followeeId, null, null, 10, "test", "name", Direction.DESCENDING);

    // then
    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("팔로워 목록 조회 - createdAt 정렬")
  void findFollowersWithCursor_orderByCreatedAt() {
    // given
    given(jpaQuery.fetch()).willReturn(List.of(follow));

    // when
    List<Follow> result = followRepositoryCustomImpl.findFollowersWithCursor(
        followeeId, null, null, 10, null, "createdAt", Direction.DESCENDING);

    // then
    assertThat(result).hasSize(1);
  }
}
