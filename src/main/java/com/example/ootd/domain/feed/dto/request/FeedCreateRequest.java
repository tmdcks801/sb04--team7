package com.example.ootd.domain.feed.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "피드 등록 요청")
public record FeedCreateRequest(
    @Schema(description = "작성자 id")
    UUID authorId,
    @Schema(description = "날씨 id")
    UUID weatherId,
    @Schema(description = "의상 id 목록")
    List<UUID> clothesIds,
    @Schema(description = "피드 내용")
    String content
) {

}
