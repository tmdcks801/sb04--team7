package com.example.ootd.domain.follow.repository;

import com.example.ootd.domain.follow.entity.Follow;
import com.example.ootd.domain.follow.repository.custom.FollowRepositoryCustom;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID>, FollowRepositoryCustom {

  boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

  long countByFollowerId(UUID followerId);

  long countByFolloweeId(UUID followeeId);
}
