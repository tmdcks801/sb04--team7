package com.example.ootd.domain.follow.repository.custom.impl;

import com.example.ootd.domain.follow.dto.Direction;
import com.example.ootd.domain.follow.entity.Follow;
import com.example.ootd.domain.follow.entity.QFollow;
import com.example.ootd.domain.follow.repository.custom.FollowRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@RequiredArgsConstructor
@Repository
public class FollowRepositoryCustomImpl implements FollowRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  QFollow follow = QFollow.follow;

  /**
   * 팔로우 목록을 커서 기반으로 조회
   *
   * @param followingId 팔로우하는 사람의 ID
   * @param cursor 커서 값
   * @param idAfter 커서 이후의 ID
   * @param limit 조회할 최대 개수
   * @param nameLike 이름 검색 조건
   * @param orderBy
   * @param direction
   * @return followings
   */
  @Override
  public List<Follow> findFollowingsWithCursor(UUID followingId, String cursor, UUID idAfter,
      int limit, String nameLike, String orderBy, Direction direction) {

    BooleanBuilder booleanBuilder = new BooleanBuilder();

    // 키워드 검색 - 팔로우하는 사람의 이름
    if (nameLike != null && !nameLike.isEmpty()) {
      booleanBuilder.and(follow.followee.name.containsIgnoreCase(nameLike));
    }

    // 팔로우하는 사람의 ID로 필터링
    if( followingId != null) {
      booleanBuilder.and(follow.follower.id.eq(followingId));
    }

    // 커서 이후의 ID로 필터링
    if (cursor != null && idAfter != null) {
      booleanBuilder.and(follow.followee.id.gt(idAfter));
    }

    // 팔로우 목록을 조회하고, 최대 limit 개수만큼 반환
    List<Follow> followings = queryFactory
        .selectFrom(follow)
        .leftJoin(follow.followee).fetchJoin()
        .where(booleanBuilder)
        .orderBy(getFolloweeOrderSpecifier(orderBy, direction))
        .limit(limit)
        .fetch();

    log.info("팔로잉 목록 조회: 팔로우하는 사람 ID: {}, 커서: {}, 이후 ID: {}, 조회 개수: {}, 이름 검색 조건: {}",
        followingId, cursor, idAfter, limit, nameLike);

    if (followings.isEmpty()) {
      log.info("팔로잉 목록이 비어 있습니다. 팔로우하는 사람 ID: {}, 커서: {}, 이후 ID: {}, 조회 개수: {}, 이름 검색 조건: {}",
          followingId, cursor, idAfter, limit, nameLike);
    } else {
      log.info("팔로우 목록 조회 성공: {}개의 팔로우를 찾았습니다.", followings.size());
    }

    return followings;
  }

  /**
   * 팔로워 목록을 커서 기반으로 조회
   *
   * @param followerId 팔로우하는 사람의 ID
   * @param cursor 커서 값
   * @param idAfter 커서 이후의 ID
   * @param limit 조회할 최대 개수
   * @param nameLike 이름 검색 조건
   * @param orderBy
   * @param direction
   * @return followers
   */
  @Override
  public List<Follow> findFollowersWithCursor(UUID followerId, String cursor, UUID idAfter,
      int limit, String nameLike, String orderBy, Direction direction) {

    BooleanBuilder booleanBuilder = new BooleanBuilder();

    // 키워드 검색 - 팔로우하는 사람의 이름
    if (nameLike != null && !nameLike.isEmpty()) {
      booleanBuilder.and(follow.follower.name.containsIgnoreCase(nameLike));
    }

    // 팔로우하는 사람의 ID로 필터링
    if (followerId != null) {
      booleanBuilder.and(follow.followee.id.eq(followerId));
    }

    // 커서 이후의 ID로 필터링
    if (cursor != null && idAfter != null) {
      booleanBuilder.and(follow.follower.id.gt(idAfter));
    }

    // 팔로워 목록을 조회하고, 최대 limit 개수만큼 반환
    List<Follow> followers = queryFactory
        .selectFrom(follow)
        .leftJoin(follow.follower).fetchJoin()
        .where(booleanBuilder)
        .orderBy(getFollowerOrderSpecifier(orderBy, direction))
        .limit(limit)
        .fetch();

    log.info("팔로워 목록 조회: 팔로우하는 사람 ID: {}, 커서: {}, 이후 ID: {}, 조회 개수: {}, 이름 검색 조건: {}",
        followerId, cursor, idAfter, limit, nameLike);

    if (followers.isEmpty()) {
      log.info("팔로워 목록이 비어 있습니다. 팔로우하는 사람 ID: {}, 커서: {}, 이후 ID: {}, 조회 개수: {}, 이름 검색 조건: {}",
          followerId, cursor, idAfter, limit, nameLike);
    } else {
      log.info("팔로워 목록 조회 성공: {}개의 팔로우를 찾았습니다.", followers.size());
    }

    return followers;
  }

  private OrderSpecifier<?> getFolloweeOrderSpecifier(String orderBy, Direction direction) {
    boolean isAsc = direction == Direction.ASCENDING;

    switch (orderBy) {
      case "createdAt":
        return isAsc ? follow.followee.createdAt.asc() : follow.followee.createdAt.desc();
      case "name":
        return isAsc ? follow.followee.name.asc() : follow.followee.name.desc();
      default:
        return follow.createdAt.asc();
    }
  }

  private OrderSpecifier<?> getFollowerOrderSpecifier(String orderBy, Direction direction) {
    boolean isAsc = direction == Direction.ASCENDING;

    switch (orderBy) {
      case "createdAt":
        return isAsc ? follow.follower.createdAt.asc() : follow.follower.createdAt.desc();
      case "name":
        return isAsc ? follow.follower.name.asc() : follow.follower.name.desc();
      default:
        return follow.createdAt.asc();
    }
  }
}
