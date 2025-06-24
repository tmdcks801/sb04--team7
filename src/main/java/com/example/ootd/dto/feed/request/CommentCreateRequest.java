package com.example.ootd.dto.feed.request;

import java.util.UUID;

public record CommentCreateRequest(
    UUID feadId,
    UUID authorId,
    String content
) {

}
