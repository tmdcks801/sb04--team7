package com.example.ootd.domain.follow.repository;

import com.example.ootd.domain.follow.entity.Follow;
import com.example.ootd.domain.follow.repository.custom.FollowRepositoryCustom;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID>, FollowRepositoryCustom {

  boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

  long countByFollowerId(UUID followerId);

  long countByFolloweeId(UUID followeeId);

  // 해당 유저를 팔로우하고 있는 모든 팔로워의 id 조회
  @Query("select f.follower.id from Follow f where f.followee.id = :followeeId")
  List<UUID> findFollowersByFolloweeId(@Param("followeeId") UUID followeeId);
}
