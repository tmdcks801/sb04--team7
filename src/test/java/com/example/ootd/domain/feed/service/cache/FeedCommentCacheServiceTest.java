package com.example.ootd.domain.feed.service.cache;

import com.example.ootd.domain.feed.mapper.CommentMapper;
import com.example.ootd.domain.feed.repository.FeedCommentRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@ExtendWith(MockitoExtension.class)
public class FeedCommentCacheServiceTest {

  @Mock
  private FeedCommentRepository feedCommentRepository;
  @Mock
  private CommentMapper commentMapper;
  @Mock
  private CacheManager cacheManager;
  @Mock
  private Cache cache;

  @InjectMocks
  private FeedCommentCacheService feedCommentCacheService;
}
