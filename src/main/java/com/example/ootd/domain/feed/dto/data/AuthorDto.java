package com.example.ootd.domain.feed.dto.data;

import java.util.UUID;
import lombok.Builder;

@Builder
public record AuthorDto(
    UUID userId,
    String name,
    String profileImageUrl
) {

}
