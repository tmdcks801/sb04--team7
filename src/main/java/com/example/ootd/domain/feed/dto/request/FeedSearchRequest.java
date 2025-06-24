package com.example.ootd.domain.feed.dto.request;

import java.util.UUID;

public record FeedSearchRequest(
    String cursor,
    UUID idAfter,
    Integer limit,
    String sortBy,
    String sortDirection,
    String keywordLike,
    String skyStatusEqual,
    String precipitationTypeEqual,
    UUID authorIdEqual
) {

}
