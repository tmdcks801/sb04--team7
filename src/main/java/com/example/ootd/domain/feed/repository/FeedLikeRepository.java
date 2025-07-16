package com.example.ootd.domain.feed.repository;

import com.example.ootd.domain.feed.entity.FeedLike;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedLikeRepository extends JpaRepository<FeedLike, UUID> {

  @Query("""
      SELECT fl
        FROM FeedLike fl
       WHERE fl.feed.id = :feedId
             AND fl.user.id = :userId
      """)
  Optional<FeedLike> findByFeedIdAndUserId(
      @Param("feedId") UUID feedId,
      @Param("userId") UUID userId
  );

  @Query("""
      SELECT fl
        FROM FeedLike fl
       WHERE fl.user.id = :userId
      """)
  List<FeedLike> findAllByUserId(@Param("userId") UUID userId);

  @Query("""
      SELECT f.id, COUNT(fl) 
        FROM Feed f
        LEFT JOIN FeedLike fl ON fl.feed.id = f.id
       WHERE f.id IN :feedIds
       GROUP BY f.id
      """)
  List<Object[]> countLikesByFeedIds(@Param("feedIds") List<UUID> feedIds);

  long countByFeedId(UUID feedId);
}
