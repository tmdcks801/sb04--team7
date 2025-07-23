package com.example.ootd.domain.feed.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "댓글 등록 요청")
public record CommentCreateRequest(
    @Schema(description = "피드 id")
    UUID feedId,
    @Schema(description = "작성자 id")
    UUID authorId,
    @Schema(description = "댓글 내용")
    String content
) {

}
