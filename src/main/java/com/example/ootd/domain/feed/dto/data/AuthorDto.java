package com.example.ootd.domain.feed.dto.data;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "작성자 정보")
public record AuthorDto(
    @Schema(description = "작성자 id")
    UUID userId,
    @Schema(description = "작성자 이름")
    String name,
    @Schema(description = "작성자 프로필 사진 url")
    String profileImageUrl
) {

}
