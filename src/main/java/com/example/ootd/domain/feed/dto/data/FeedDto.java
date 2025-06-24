package com.example.ootd.domain.feed.dto.data;

import com.example.ootd.domain.clothes.dto.data.OotdDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record FeedDto(
    UUID id,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
  // TODO: user, weather 추가 후 주석 해제
//    User author,
//    Weather weather,
    List<OotdDto> ootds,
    String content,
    long likeCount,
    int commentCount,
    boolean likedByMe
) {

}
