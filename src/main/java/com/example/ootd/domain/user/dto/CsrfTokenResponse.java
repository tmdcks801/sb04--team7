package com.example.ootd.domain.user.dto;

public record CsrfTokenResponse(
    String token,
    String parameterName,
    String headerName
) {

}
