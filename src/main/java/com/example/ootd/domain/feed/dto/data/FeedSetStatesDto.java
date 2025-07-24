package com.example.ootd.domain.feed.dto.data;

import com.example.ootd.domain.feed.entity.Feed;

// 피드 좋아요/댓글 수 업데이트용 dto
public record FeedSetStatesDto(
    Feed feed,
    long currentLikeCount,
    long currentCommentCount
) {

}
