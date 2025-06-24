package com.example.ootd.domain.user.dto;

import com.example.ootd.domain.user.UserRole;
import java.util.UUID;

public record UserSearchCondition(
    String cursor,
    UUID idAfter,
    int limit,
    String sortBy,
    String sortDirection,
    String emailLike,
    UserRole roleEqual,
    Boolean locked
) {

}
