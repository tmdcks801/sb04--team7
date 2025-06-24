package com.example.ootd.domain.feed.dto.request;

import java.util.UUID;

public record CommentCreateRequest(
    UUID feadId,
    UUID authorId,
    String content
) {

}
