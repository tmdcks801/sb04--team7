package com.example.ootd.domain.user.service;

import com.example.ootd.domain.location.Location;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.dto.ProfileDto;
import com.example.ootd.domain.user.dto.ProfileUpdateRequest;
import com.example.ootd.domain.user.dto.UserDto;
import com.example.ootd.domain.user.dto.UserPagedResponse;
import com.example.ootd.domain.user.dto.UserSearchCondition;
import com.example.ootd.domain.user.mapper.UserMapper;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.OotdException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

  private final UserRepository userRepository;
  private final UserMapper mapper;

  @Override
  @Transactional
  public UserPagedResponse getUsers(UserSearchCondition condition) {

    long totalCount = userRepository.count();
    List<User> users = userRepository.searchUserOfCondition(condition);

    if(users.isEmpty()) {
      return mapper.toPaginatedResponse(
          List.of(),
          null,
          null,
          false,
          totalCount,
          condition
      );
    }

    boolean hasNext = hasNext(condition.limit() + 1, users.size());

    if(hasNext) users.remove(users.size() - 1);
    List<UserDto> userDtos = mapper.toDtoList(users);

    UUID nextIdAfter = hasNext ? users.get(users.size() - 1).getId() : null;

    LocalDateTime nextCursor = hasNext ? calculateNextCursor(condition.sortDirection(), userDtos) : null;

    return mapper.toPaginatedResponse(
        userDtos,
        nextCursor != null ? nextCursor.toString() : null,
        nextIdAfter,
        hasNext,
        totalCount,
        condition
    );
  }

  @Override
  @Transactional(readOnly = true)
  public ProfileDto getUserProfile(UUID userId) {
    User user = userRepository.findByIdWithLocationAndImage(userId).orElseThrow(() -> new OotdException(ErrorCode.USER_NOT_FOUND));
    return mapper.toProfileDto(user);
  }

  @Override
  public ProfileDto updateUserProfile(UUID userId, ProfileUpdateRequest request,
      MultipartFile profile) {
    return null;
  }

  private boolean hasNext(int limit, int paginatedSize){
    return limit == paginatedSize;
  }

  private LocalDateTime calculateNextCursor(String sortDirection, List<UserDto> users){
    return users.stream()
        .map(UserDto::createdAt)
        .sorted(("DESCENDING".equalsIgnoreCase(sortDirection)
            ? Comparator.reverseOrder()
            : Comparator.naturalOrder()))
        .findFirst().orElse(null);
  }
}
