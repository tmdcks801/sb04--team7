package com.example.ootd.domain.user.mapper;


import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.dto.AuthorDto;
import com.example.ootd.domain.user.dto.ProfileDto;
import com.example.ootd.domain.user.dto.UserCreateRequest;
import com.example.ootd.domain.user.dto.UserDto;
import com.example.ootd.domain.user.dto.UserPagedResponse;
import com.example.ootd.domain.user.dto.UserSearchCondition;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Builder;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = false))
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

//  @Mapping(target = "id", ignore = true)
//  @Mapping(target = "image", ignore = true)
//  @Mapping(target = "location", ignore = true)
  @Mapping(target = "name", source = "request.name")
  @Mapping(target = "email", source = "request.email")
//  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "role", ignore = true)
  @Mapping(target = "provider", ignore = true)
  @Mapping(target = "providerId", ignore = true)
  @Mapping(target = "isLocked", ignore = true)
  @Mapping(target = "password", source = "request.password", qualifiedByName = "encodePassword")
  User toEntity(UserCreateRequest request, @Context PasswordEncoder passwordEncoder);

  @Mapping(target = "linkedOAuthProviders", expression = "java(user.getProvider() == null ? java.util.List.of() : java.util.List.of(user.getProvider()))")
  UserDto toDto(User user);

  List<UserDto> toDtoList(List<User> users);

  @Named("encodePassword")
  default String encodePassword(String rawPassword, @Context PasswordEncoder encoder) {
    return encoder.encode(rawPassword);
  }

  @Mapping(source = "condition.sortBy", target = "sortBy")
  @Mapping(source = "condition.sortDirection", target = "sortDirection")
  UserPagedResponse toPaginatedResponse(List<UserDto> data, String nextCursor, UUID nextIdAfter,
      boolean hasNext, Long totalCount, UserSearchCondition condition);


  @Mapping(target = "userId", source = "id")
  @Mapping(target = "profileImageUrl", source = "image.url")
  ProfileDto toProfileDto(User user);

  @Mapping(target = "userId", source = "id")
  @Mapping(target = "profileImageUrl", source = "image.url")
  AuthorDto toAuthorDto(User user);
}
