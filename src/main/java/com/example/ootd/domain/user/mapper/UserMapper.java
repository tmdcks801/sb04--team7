package com.example.ootd.domain.user.mapper;


import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.dto.UserCreateRequest;
import com.example.ootd.domain.user.dto.UserDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

  private final PasswordEncoder passwordEncoder;
  public User toEntity(UserCreateRequest request){
    return new User(request.name(), request.email(), passwordEncoder.encode(request.password()));
  }

  public UserDto toDto(User user) {
    return new UserDto(
        user.getId(),
        user.getCreatedAt(),
        user.getEmail(),
        user.getName(),
        user.getRole(),
        user.getProvider() == null ? List.of() : List.of(user.getProvider()), // TODO : 추후 변경
        user.getIsLocked()
    );
  }
}
