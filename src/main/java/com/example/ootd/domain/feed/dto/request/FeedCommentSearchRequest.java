package com.example.ootd.domain.feed.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record FeedCommentSearchRequest(
    String cursor,
    UUID idAfter,
    @NotBlank(message = "limit은 필수입니다.")
    Integer limit
) {

}
