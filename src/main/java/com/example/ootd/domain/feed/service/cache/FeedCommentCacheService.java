package com.example.ootd.domain.feed.service.cache;

import com.example.ootd.domain.feed.dto.data.CommentDto;
import com.example.ootd.domain.feed.dto.request.FeedCommentSearchCondition;
import com.example.ootd.domain.feed.entity.FeedComment;
import com.example.ootd.domain.feed.mapper.CommentMapper;
import com.example.ootd.domain.feed.repository.FeedCommentRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "feed_comment")
public class FeedCommentCacheService {

  private final FeedCommentRepository feedCommentRepository;
  private final CommentMapper commentMapper;
  private final CacheManager cacheManager;

  // 댓글 조회 결과 캐시 저장
  @Cacheable(key = "'feedId=' + #feedId + #condition.toSimpleKey()")
  public List<CommentDto> getCachedComments(UUID feedId,
      FeedCommentSearchCondition condition) {

    log.debug("댓글 캐시 MISS 발생 - feedId={}, condition={}", feedId, condition);

    saveKey(feedId, condition);

    List<FeedComment> comments = feedCommentRepository.findByCondition(condition, feedId);

    return commentMapper.toDto(comments);
  }

  // 댓글 개수 캐시 저장
  @Cacheable(key = "#feedId")
  public long getCachedCommentsCount(UUID feedId) {

    log.debug("댓글(개수) 캐시 MISS 발생 - feedId={}", feedId);

    return feedCommentRepository.countByFeedId(feedId);
  }

  public void deleteAllCommentCacheByFeedId(UUID feedId) {

    Set<String> keys = cacheManager.getCache("feed_comment_key").get(feedId, Set.class);
    Cache commentCache = cacheManager.getCache("feed_comment");

    if (commentCache != null && keys != null) {
      for (String key : keys) {
        commentCache.evict(key);
      }
      commentCache.evict(feedId);   // 댓글 캐시 삭제 시 댓글 개수 캐시도 삭제
    }

    cacheManager.getCache("feed_comment_key").evict(feedId);
  }

  // feed_comment의 키들 저장
  private void saveKey(UUID feedId, FeedCommentSearchCondition condition) {

    String key = "feedId=" + feedId + condition.toSimpleKey();

    // 저장된 키 등록
    Set<String> existingKeys = cacheManager.getCache("feed_comment_key")
        .get(feedId, Set.class);

    if (existingKeys == null) {
      existingKeys = new HashSet<>();
    }
    existingKeys.add(key);
    cacheManager.getCache("feed_comment_key").put(feedId, existingKeys);
  }
}
