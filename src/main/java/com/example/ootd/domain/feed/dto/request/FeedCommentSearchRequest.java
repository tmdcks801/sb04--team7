package com.example.ootd.domain.feed.dto.request;

import java.util.UUID;

public record FeedCommentSearchRequest(
    String cursor,
    UUID idAfter,
    Integer limit
) {

}
