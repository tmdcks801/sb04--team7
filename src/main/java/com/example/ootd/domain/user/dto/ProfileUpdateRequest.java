package com.example.ootd.domain.user.dto;

import com.example.ootd.domain.location.Location;
import com.example.ootd.domain.user.Gender;
import java.time.LocalDate;

public record ProfileUpdateRequest(
    String name,
    Gender gender,
    LocalDate birthDate,
    Location location,
    int temperatureSensitivity
) {
}
