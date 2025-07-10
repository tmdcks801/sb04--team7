package com.example.ootd.domain.user.service;

import com.example.ootd.domain.image.entity.Image;
import com.example.ootd.domain.image.service.ImageService;
import com.example.ootd.domain.location.Location;
import com.example.ootd.domain.location.dto.WeatherAPILocation;
import com.example.ootd.domain.location.repository.LocationRepository;
import com.example.ootd.domain.location.service.LocationService;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.dto.ChangePasswordRequest;
import com.example.ootd.domain.user.dto.ProfileDto;
import com.example.ootd.domain.user.dto.ProfileUpdateRequest;
import com.example.ootd.domain.user.dto.UserDto;
import com.example.ootd.domain.user.dto.UserLockUpdateRequest;
import com.example.ootd.domain.user.dto.UserPagedResponse;
import com.example.ootd.domain.user.dto.UserRoleUpdateRequest;
import com.example.ootd.domain.user.dto.UserSearchCondition;
import com.example.ootd.domain.user.mapper.UserMapper;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.OotdException;
import com.example.ootd.security.jwt.JwtSessionRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

  private final UserRepository userRepository;
  private final LocationRepository locationRepository;
  private final UserMapper mapper;
  private final JwtSessionRepository jwtSessionRepository;
  private final ImageService imageService;
  private final PasswordEncoder encoder;

  private final LocationService locationService;
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
  @Transactional
  public UserDto changeUserRole(UserRoleUpdateRequest request, UUID userId){


    User user = userRepository.findById(userId).orElseThrow(() -> new OotdException(ErrorCode.USER_NOT_FOUND));
    user.updateRole(request.role());

    jwtSessionRepository.deleteAllByUser_Id(userId); // TODO : 현재 SRP 위반, 추후 분리

    return mapper.toDto(user);
  }


  @Override
  @Transactional
  public ProfileDto updateUserProfile(UUID userId, ProfileUpdateRequest req, MultipartFile image){


    // locationRepository.save(req.location());
    locationService.getGridAndLocation(req.location().getLatitude(), req.location().getLongitude());
    Location location = locationRepository.findByLatitudeAndLongitude(req.location().getLatitude(), req.location().getLongitude());


    User user = userRepository.findById(userId).orElseThrow(() -> new OotdException(ErrorCode.USER_NOT_FOUND));

    Image profileImage = imageService.upload(image); // TODO : 비동기 처리 고려. 업로드 완료시 이벤트 발행?
    user.updateProfile(req, profileImage);
    user.updateLocation(location); // TODO : 업데이트 로직 더 깔끔하게 작성


    return mapper.toProfileDto(user);
  }

  @Override
  @Transactional
  public void updateUserPassword(UUID userId, ChangePasswordRequest request){
    User user = userRepository.findById(userId).orElseThrow(() -> new OotdException(ErrorCode.USER_NOT_FOUND));
    user.updatePassword(encoder.encode(request.password()));
  }

  @Override
  @Transactional
  public void updateUserLockStatus(UUID userId, UserLockUpdateRequest request){
    User user = userRepository.findById(userId).orElseThrow(() -> new OotdException(ErrorCode.USER_NOT_FOUND));
    user.updateLockStatus(request.locked());
    if(request.locked()) jwtSessionRepository.deleteAllByUser_Id(userId);
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
