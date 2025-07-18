package com.example.ootd.domain.feed.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "댓글 검색 조건")
public record FeedCommentSearchCondition(
    @Schema(description = "커서 - 이전 페이지 마지막 요소 createdAt")
    LocalDateTime cursor,
    @Schema(description = "보조 커서 - 이전 페이지 마지막 요소 id")
    UUID idAfter,
    @NotNull(message = "limit은 필수입니다.")
    @Schema(description = "조회할 항목 수")
    int limit
) {

  // 캐시 키값 반환
  public String toSimpleKey() {
    return String.format(
        ":cursor=%s:idAfter=%s:limit=%s",
        nullToStr(cursor),
        nullToStr(idAfter),
        limit
    );
  }

  private String nullToStr(Object o) {
    return o == null ? "null" : o.toString();
  }
}
