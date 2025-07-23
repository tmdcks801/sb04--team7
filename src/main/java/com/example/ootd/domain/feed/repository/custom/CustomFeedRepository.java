package com.example.ootd.domain.feed.repository.custom;

import com.example.ootd.domain.feed.dto.request.FeedSearchCondition;
import com.example.ootd.domain.feed.entity.Feed;
import com.example.ootd.domain.feed.entity.FeedClothes;
import java.util.List;
import java.util.UUID;

public interface CustomFeedRepository {

  // 조건에 해당하는 피드 검색
  List<Feed> findByCondition(FeedSearchCondition condition);

  // 조회된 피드의 옷들 검색
  List<FeedClothes> findFeedClothesByFeedIds(List<UUID> feedIds);

  // 해당 피드의 옷들 검색
  List<FeedClothes> findFeedClothesByFeedId(UUID feedId);

  // 조건에 맞는 피드 개수
  long countByCondition(FeedSearchCondition condition);
}
