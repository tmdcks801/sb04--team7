package com.example.ootd.domain.user.dto;

import com.example.ootd.domain.user.UserRole;
import com.example.ootd.security.Provider;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserDto(
    UUID id,
    LocalDateTime createdAt,
    String email,
    String name,
    UserRole role,
    List<Provider> linkedOAuthProviders,
    boolean locked
) {

}
