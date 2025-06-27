package com.example.ootd.domain.user.dto;

import lombok.Getter;

public record LoginDto(
    String email,
    String password
) {

}
