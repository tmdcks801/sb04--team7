package com.example.ootd.domain.feed.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "피드 수정 요청")
public record FeedUpdateRequest(
    @Schema(description = "피드 내용")
    String content
) {

}
