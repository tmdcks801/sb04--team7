package com.example.ootd.domain.feed.dto.data;

import com.example.ootd.domain.clothes.dto.data.OotdDto;
import com.example.ootd.domain.user.dto.AuthorDto;
import com.example.ootd.domain.weather.dto.WeatherSummaryDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record FeedDto(
    UUID id,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    AuthorDto author,
    WeatherSummaryDto weather,
    List<OotdDto> ootds,
    String content,
    long likeCount,
    int commentCount,
    boolean likedByMe
) {

}
