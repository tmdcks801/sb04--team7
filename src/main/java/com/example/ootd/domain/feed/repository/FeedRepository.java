package com.example.ootd.domain.feed.repository;

import com.example.ootd.domain.feed.dto.data.FeedCountDto;
import com.example.ootd.domain.feed.dto.data.FeedSetStatesDto;
import com.example.ootd.domain.feed.entity.Feed;
import com.example.ootd.domain.feed.repository.custom.CustomFeedRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedRepository extends JpaRepository<Feed, UUID>, CustomFeedRepository {

  @Query("""
      SELECT new com.example.ootd.domain.feed.dto.data.FeedSetStatesDto(f, COUNT(DISTINCT fl), COUNT(DISTINCT fc))
        FROM Feed f
        LEFT JOIN FeedLike fl ON fl.feed.id = f.id
        LEFT JOIN FeedComment fc ON fc.feed.id = f.id
       GROUP BY f.id
      """)
  List<FeedSetStatesDto> countLikesAndComments();

  @Query("""
      SELECT new com.example.ootd.domain.feed.dto.data.FeedCountDto(f.id, COUNT(DISTINCT fl), COUNT(DISTINCT fc))
        FROM Feed f
        LEFT JOIN FeedLike fl ON fl.feed.id = f.id
        LEFT JOIN FeedComment fc ON fc.feed.id = f.id
       WHERE f.id IN :feedIds
       GROUP BY f.id
      """)
  List<FeedCountDto> countLikesAndCommentsByFeedIds(@Param("feedIds") List<UUID> feedIds);

  @Query("""
      SELECT new com.example.ootd.domain.feed.dto.data.FeedCountDto(f.id, COUNT(DISTINCT fl), COUNT(DISTINCT fc))
        FROM Feed f
        LEFT JOIN FeedLike fl ON fl.feed.id = f.id
        LEFT JOIN FeedComment fc ON fc.feed.id = f.id
       WHERE f.id = :feedId
       GROUP BY f.id
      """)
  FeedCountDto countLikeAndCommentByFeedId(@Param("feedId") UUID feedId);
}
