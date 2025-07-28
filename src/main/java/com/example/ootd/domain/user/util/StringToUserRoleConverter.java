package com.example.ootd.domain.user.util;

import com.example.ootd.domain.user.UserRole;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToUserRoleConverter implements Converter<String, UserRole> {

  @Override
  public UserRole convert(String source) {
    if (source == null) return null;
    if (source.startsWith("ROLE_")) {
      return UserRole.valueOf(source);
    }
    return UserRole.valueOf("ROLE_" + source);
  }
}
