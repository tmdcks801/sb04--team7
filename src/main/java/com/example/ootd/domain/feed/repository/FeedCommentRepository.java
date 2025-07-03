package com.example.ootd.domain.feed.repository;

import com.example.ootd.domain.feed.entity.FeedComment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedCommentRepository extends JpaRepository<FeedComment, UUID> {

  @Query("""
      SELECT fc
        FROM FeedComment fc
       WHERE fc.feed.id = :feedId 
             AND fc.createdAt < :cursor
             AND fc.id > :idAfter
       ORDER BY fc.createdAt desc, fc.id asc
      """)
  List<FeedComment> findByCondition(
      @Param("feedId") UUID feedId,
      @Param("cursor") LocalDateTime cursor,
      @Param("idAfter") UUID idAfter,
      Pageable pageable
  );

  @Query("""
      SELECT count(fc.id)
        FROM FeedComment fc
       WHERE fc.feed.id = :feedId
      """)
  long countByCondition(@Param("feedId") UUID feedId);
}
