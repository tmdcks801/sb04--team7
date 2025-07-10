package com.example.ootd.domain.message.repository;

import com.example.ootd.domain.message.entity.Message;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface MessageRepository extends JpaRepository<Message, UUID> {

  List<Message> findByDmKey(String dmKey, Pageable pageable);

  @Query("""
      SELECT m FROM Message m
      WHERE m.dmKey = :dmKey
        AND ( m.createdAt < :pivotTime
              OR (m.createdAt = :pivotTime AND m.id < :pivotId) )
      """)
  List<Message> findOlderThan(
      @Param("dmKey") String dmKey,
      @Param("pivotTime") LocalDateTime pivotTime,
      @Param("pivotId") UUID pivotId,
      Pageable pageable);

  @Query("""
      SELECT m FROM Message m
      WHERE m.dmKey = :dmKey
        AND ( m.createdAt > :pivotTime
              OR (m.createdAt = :pivotTime AND m.id > :pivotId) )
      """)
  List<Message> findNewerThan(
      @Param("dmKey") String dmKey,
      @Param("pivotTime") LocalDateTime pivotTime,
      @Param("pivotId") UUID pivotId,
      Pageable pageable);

  Long countByDmKey(String dmKey);
}
