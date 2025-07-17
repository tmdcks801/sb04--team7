package com.example.ootd.config.api.dto;

import com.example.ootd.domain.feed.dto.data.CommentDto;
import com.example.ootd.domain.feed.dto.data.FeedDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "피드 목록 응답")
public record FeedPageResponse(
    @Schema(description = "피드 리스트")
    List<FeedDto> data,
    @Schema(description = "다음 페이지 유무")
    boolean hasNext,
    @Schema(description = "다음 페이지에서 사용할 커서")
    Object nextCursor,
    @Schema(description = "다음 페이지에서 사용할 보조 커서")
    UUID nextIdAfter,
    @Schema(description = "정렬 기준")
    String sortBy,
    @Schema(description = "정렬 방향")
    String sortDirection,
    @Schema(description = "피드 총 개수")
    long totalCount
) {

}
