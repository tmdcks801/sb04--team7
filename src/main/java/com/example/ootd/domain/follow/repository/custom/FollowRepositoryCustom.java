package com.example.ootd.domain.follow.repository.custom;

import com.example.ootd.domain.follow.dto.Direction;
import com.example.ootd.domain.follow.entity.Follow;
import java.util.List;
import java.util.UUID;

public interface FollowRepositoryCustom {

  /**
   * 팔로우 목록을 커서 기반으로 조회
   *
   * @param followingId 팔로우하는 사람의 ID
   * @param cursor 커서 값
   * @param idAfter 커서 이후의 ID
   * @param limit 조회할 최대 개수
   * @param nameLike 이름 검색 조건
   * @return 팔로우 목록
   */
  List<Follow> findFollowingsWithCursor(
      UUID followingId,
      String cursor,
      UUID idAfter,
      int limit,
      String nameLike,
      String orderBy,
      Direction direction
  );

  /**
   * 팔로워 목록을 커서 기반으로 조회
   *
   * @param followerId 팔로우하는 사람의 ID
   * @param cursor 커서 값
   * @param idAfter 커서 이후의 ID
   * @param limit 조회할 최대 개수
   * @param nameLike 이름 검색 조건
   * @return 팔로워 목록
   */
  List<Follow> findFollowersWithCursor(
      UUID followerId,
      String cursor,
      UUID idAfter,
      int limit,
      String nameLike,
      String orderBy,
      Direction direction
  );
}
