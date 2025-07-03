package com.example.ootd.domain.feed.repository;

import com.example.ootd.domain.feed.dto.request.FeedSearchCondition;
import com.example.ootd.domain.feed.entity.Feed;
import java.util.List;

public interface CustomFeedRepository {

  // 조건에 해당하는 피드 검색
  List<Feed> findByCondition(FeedSearchCondition condition);

  // 조건에 맞는 피드 개수
  long countByCondition(FeedSearchCondition condition);
}
