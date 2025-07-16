package com.example.ootd.domain.feed.service.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "feed_comment")
public class FeedCommentCacheService {

}
