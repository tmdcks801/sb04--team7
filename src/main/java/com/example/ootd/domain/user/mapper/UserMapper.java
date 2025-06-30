package com.example.ootd.domain.user.mapper;


import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.dto.UserCreateRequest;
import com.example.ootd.domain.user.dto.UserDto;
import com.example.ootd.domain.user.dto.UserPagedResponse;
import com.example.ootd.domain.user.dto.UserSearchCondition;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface UserMapper {

//  private final PasswordEncoder passwordEncoder;
//  public User toEntity(UserCreateRequest request){
//    return new User(request.name(), request.email(), passwordEncoder.encode(request.password()));
//  }
//
//  public UserDto toDto(User user) {
//    return new UserDto(
//        user.getId(),
//        user.getCreatedAt(),
//        user.getEmail(),
//        user.getName(),
//        user.getRole(),
//        user.getProvider() == null ? List.of() : List.of(user.getProvider()), // TODO : 추후 변경
//        user.getIsLocked()
//    );
//  }

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "image", ignore = true)
  @Mapping(target = "location", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "role", defaultValue = "ROLE_USER")
  @Mapping(target = "provider", ignore = true)
  @Mapping(target = "providerId", ignore = true)
  @Mapping(target = "isLocked", defaultValue = "false")
  @Mapping(target = "password", expression = "java(encodePassword(request.password(), passwordEncoder))")
  User toEntity(UserCreateRequest request, @Context PasswordEncoder passwordEncoder);

  @Mapping(target = "providers", expression = "java(user.getProvider() == null ? java.util.List.of() : java.util.List.of(user.getProvider()))")
  UserDto toDto(User user);

  List<UserDto> toDtoList(List<User> users);

  default String encodePassword(String rawPassword, @Context PasswordEncoder encoder) {
    return encoder.encode(rawPassword);
  }

  @Mapping(source = "condition.sortBy", target = "sortBy")
  @Mapping(source = "condition.sortDirection", target = "sortDirection")
  UserPagedResponse toPaginatedResponse(List<UserDto> data, String nextCursor, UUID nextIdAfter, boolean hasNext, Long totalCount, UserSearchCondition condition);

}
