package com.example.ootd.domain.feed.dto.data;

import com.example.ootd.domain.clothes.dto.data.OotdDto;
import com.example.ootd.domain.user.dto.AuthorDto;
import com.example.ootd.domain.weather.dto.WeatherSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "피드 정보")
public record FeedDto(
    @Schema(description = "피드 id")
    UUID id,
    @Schema(description = "피드 작성일")
    LocalDateTime createdAt,
    @Schema(description = "피드 수정일")
    LocalDateTime updatedAt,
    @Schema(description = "작성자 정보")
    AuthorDto author,
    @Schema(description = "날씨 정보")
    WeatherSummaryDto weather,
    @Schema(description = "의상 정보 목록")
    List<OotdDto> ootds,
    @Schema(description = "피드 내용")
    String content,
    @Schema(description = "좋아요 수")
    long likeCount,
    @Schema(description = "댓글 수")
    int commentCount,
    @Schema(description = "사용자가 좋아요 했는지 여부")
    boolean likedByMe
) {

}
