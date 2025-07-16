package com.example.ootd.domain.feed.dto.request;

import com.example.ootd.domain.weather.entity.PrecipitationType;
import com.example.ootd.domain.weather.entity.SkyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
public record FeedSearchCondition(
    String cursor,
    UUID idAfter,
    @NotNull(message = "limit은 필수입니다.")
    Integer limit,
    @NotBlank(message = "정렬 기준은 필수입니다.")
    String sortBy,
    @NotBlank(message = "정렬 방향은 필수입니다.")
    String sortDirection,
    String keywordLike,
    SkyStatus skyStatusEqual,
    PrecipitationType precipitationTypeEqual,
    UUID authorIdEqual
) {

  // 캐시 키값 반환
  public String toSimpleKye() {
    return String.format(
        "cursor=%s:idAfter=%s:limit=%s:sortBy=%s:sortDirection=%s:keyword=%s:sky=%s:rain=%s:author=%s",
        nullToStr(cursor),
        nullToStr(idAfter),
        limit,
        sortBy,
        sortDirection,
        nullToStr(keywordLike),
        nullToStr(skyStatusEqual),
        nullToStr(precipitationTypeEqual),
        nullToStr(authorIdEqual)
    );
  }

  private String nullToStr(Object o) {
    return o == null ? "null" : o.toString();
  }
}
