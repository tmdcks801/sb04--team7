package com.example.ootd.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PageResponse<T>(
    List<T> data,
    boolean hasNext,
    Object nextCursor,
    UUID nextIdAfter,
    String sortBy,
    String sortDirection,
    long totalCount
) {

}
