package com.example.ootd.domain.feed.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record FeedCommentSearchCondition(
    LocalDateTime cursor,
    UUID idAfter,
    @NotNull(message = "limit은 필수입니다.")
    int limit
) {

}
