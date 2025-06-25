package com.example.ootd.domain.user.service;

import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.dto.ChangePasswordRequest;
import com.example.ootd.domain.user.dto.ResetPasswordRequest;
import com.example.ootd.domain.user.dto.UserCreateRequest;
import com.example.ootd.domain.user.dto.UserDto;
import com.example.ootd.domain.user.dto.UserLockUpdateRequest;
import com.example.ootd.domain.user.dto.UserRoleUpdateRequest;
import com.example.ootd.domain.user.mapper.UserMapper;
import com.example.ootd.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

  private final UserMapper userMapper;
  private final UserRepository userRepository;

  @Override
  public UserDto registerUser(UserCreateRequest request) {
    User newUser = userMapper.toEntity(request);
    return userMapper.toDto(userRepository.save(newUser));
  }

  @Override
  public UserDto updateUserRole(UUID userId, UserRoleUpdateRequest request) {
    return null;
  }

  @Override
  public void updateUserPassword(Authentication auth, ChangePasswordRequest request) {

  }

  @Override
  public UUID changeLockStatus(Authentication auth, UserLockUpdateRequest request) {
    return null;
  }

  @Override
  public void resetPassword(ResetPasswordRequest request) {

  }
}
