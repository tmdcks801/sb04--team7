package com.example.ootd.dto.feed.data;

import com.example.ootd.dto.clothes.data.OotdDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FeedDto(
    UUID id,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    User author,
    Weather weather,
    List<OotdDto> ootds,
    String content,
    long likeCount,
    int commentCount,
    boolean likedByMe
) {

}
