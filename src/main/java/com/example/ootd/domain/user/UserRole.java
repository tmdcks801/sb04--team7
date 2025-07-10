package com.example.ootd.domain.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {
  ROLE_USER,
  ROLE_ADMIN;

  @JsonCreator
  public static UserRole from(String value) {
    if (value == null) return null;
    if (value.startsWith("ROLE_")) {
      return UserRole.valueOf(value);
    }
    return UserRole.valueOf("ROLE_" + value);
  }

  @JsonValue
  public String toValue() {
    return this.name();
  }
}
