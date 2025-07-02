package com.example.ootd.domain.feed.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record FeedSearchCondition(
    String cursor,
    UUID idAfter,
    @NotNull(message = "limit은 필수입니다.")
    Integer limit,
    @NotBlank(message = "정렬 기준은 필수입니다.")
    String sortBy,
    @NotBlank(message = "정렬 방향은 필수입니다.")
    String sortDirection,
    String keywordLike,
    String skyStatusEqual,
    String precipitationTypeEqual,
    UUID authorIdEqual
) {

}
