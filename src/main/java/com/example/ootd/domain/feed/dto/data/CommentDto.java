package com.example.ootd.domain.feed.dto.data;

import com.example.ootd.domain.user.dto.AuthorDto;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CommentDto(
    UUID id,
    AuthorDto author,
    String content,
    LocalDateTime createdAt,
    UUID feedId
) {

}
