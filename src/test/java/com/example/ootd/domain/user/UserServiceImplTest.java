package com.example.ootd.domain.user;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

import com.example.ootd.TestEntityFactory;
import com.example.ootd.domain.image.service.ImageService;
import com.example.ootd.domain.location.Location;
import com.example.ootd.domain.location.repository.LocationRepository;
import com.example.ootd.domain.location.service.LocationService;
import com.example.ootd.domain.user.dto.ProfileDto;
import com.example.ootd.domain.user.dto.ProfileUpdateRequest;
import com.example.ootd.domain.user.dto.UserDto;
import com.example.ootd.domain.user.dto.UserPagedResponse;
import com.example.ootd.domain.user.dto.UserRoleUpdateRequest;
import com.example.ootd.domain.user.dto.UserSearchCondition;
import com.example.ootd.domain.user.mapper.UserMapper;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.domain.user.service.UserServiceImpl;
import com.example.ootd.security.jwt.JwtSessionRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

  @InjectMocks
  UserServiceImpl userService;

  @Mock
  UserRepository userRepository;
  @Mock
  LocationRepository locationRepository;
  @Mock
  JwtSessionRepository jwtSessionRepository;
  @Spy
  private UserMapper userMapper = Mappers.getMapper(UserMapper.class);
  @Mock
  ImageService imageService;
  @Mock
  PasswordEncoder encoder;
  @Mock
  LocationService locationService;

  User user1;
  User user2;

  @BeforeEach
  void setUp(){
    user1 = TestEntityFactory.createUser();
    user2 = TestEntityFactory.createUser();
  }


  @Test
  @DisplayName("limit 미만의 사용자 존재시 hasNext는 false")
  void limit_미만의_사용자_존재시_hasNext는_false(){
    // given
    UserSearchCondition condition = new UserSearchCondition(
        null,
        null,
        10,
        "createdAt",
        "DESCENDING",
        null,
        null,
        false
    );

    given(userRepository.count()).willReturn(2L);
    given(userRepository.searchUserOfCondition(condition))
        .willReturn(List.of(user1, user2));


    // when
    UserPagedResponse users = userService.getUsers(condition);

    // then
    assertThat(users.hasNext()).isFalse();
    assertThat(users.data().size()).isEqualTo(2);
  }

  @Test
  @DisplayName("limit 이상의 사용자 존재시 hasNext는 true")
  void limit_이상의_사용자_존재시_hasNext는_true(){
    // given
    UserSearchCondition condition = new UserSearchCondition(
        null,
        null,
        1,
        "createdAt",
        "DESCENDING",
        null,
        null,
        false
    );

    given(userRepository.count()).willReturn(2L);
    given(userRepository.searchUserOfCondition(condition))
        .willReturn(new ArrayList<>(List.of(user1, user2)));


    // when
    UserPagedResponse users = userService.getUsers(condition);

    // then
    assertThat(users.hasNext()).isTrue();
    assertThat(users.data().size()).isEqualTo(1);
  }


  @Test
  @DisplayName("사용자 권한을 바꿀수 있다")
  void 사용자_권한을_바꿀수_있다(){
    // given
    UserRole prevRole = user1.getRole();
    given(userRepository.findById(any()))
        .willReturn(Optional.of(user1));

    UserRoleUpdateRequest req = new UserRoleUpdateRequest(UserRole.ROLE_ADMIN);
    // when
    UserDto user1Dto = userService.changeUserRole(req, user1.getId());

    // then
    assertThat(user1Dto.role()).isEqualTo(UserRole.ROLE_ADMIN);
    assertThat(prevRole).isNotEqualTo(user1Dto.role());
  }

  @Test
  @DisplayName("사용자 프로필을 업데이트 할 수 있다 - 사진 x")
  void 사용자_프로필을_업데이트할_수_있다(){
    // given
    Location location = mock(Location.class);
    ProfileUpdateRequest request = new ProfileUpdateRequest(
        "newName",
        Gender.FEMALE,
        LocalDate.now(),
        location,
        5
    );
    given(locationRepository.findByLatitudeAndLongitude(anyDouble(), anyDouble()))
        .willReturn(location);
    given(userRepository.findById(any()))
        .willReturn(Optional.of(user1));
    given(imageService.upload(any()))
        .willReturn(null);

    // when
    ProfileDto dto = userService.updateUserProfile(user1.getId(), request, null);

    // then
    assertThat(dto.location()).isEqualTo(location);
    assertThat(dto.name()).isEqualTo("newName");
  }


}
