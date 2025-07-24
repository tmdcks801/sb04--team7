package com.example.ootd.domain.feed.dto.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.UUID;

// 피드 좋아요/댓글 수 캐시용 dto
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public record FeedCountDto(
    UUID feedId,  // 피드 id,
    long currentLikeCount, // 실제 좋아요 수
    long currentCommentCount // 실제 댓글 수
) {

}
