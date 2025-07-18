package com.example.ootd.domain.feed.dto.data;

import com.example.ootd.domain.user.dto.AuthorDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "댓글 정보")
public record CommentDto(
    @Schema(description = "댓글 id")
    UUID id,
    @Schema(description = "작성자 정보")
    AuthorDto author,
    @Schema(description = "댓글 내용")
    String content,
    @Schema(description = "댓글 작성일")
    LocalDateTime createdAt,
    @Schema(description = "피드 id")
    UUID feedId
) {

}
