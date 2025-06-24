package com.example.ootd.dto;

import java.util.List;
import java.util.UUID;

public record PageResponse<T>(
    List<T> Data,
    boolean hasNext,
    Object nextCursor,
    UUID nextIdAfter,
    String sortBy,
    String sortDirection,
    long totalCount
) {

}
