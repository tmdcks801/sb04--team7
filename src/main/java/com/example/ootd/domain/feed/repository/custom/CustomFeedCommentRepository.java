package com.example.ootd.domain.feed.repository.custom;

import com.example.ootd.domain.feed.dto.request.FeedCommentSearchCondition;
import com.example.ootd.domain.feed.entity.FeedComment;
import java.util.List;
import java.util.UUID;

public interface CustomFeedCommentRepository {

  List<FeedComment> findByCondition(FeedCommentSearchCondition condition, UUID feedId);
}
