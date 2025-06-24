package com.example.ootd.domain.user.service;

import com.example.ootd.domain.user.dto.ChangePasswordRequest;
import com.example.ootd.domain.user.dto.ResetPasswordRequest;
import com.example.ootd.domain.user.dto.UserCreateRequest;
import com.example.ootd.domain.user.dto.UserDto;
import com.example.ootd.domain.user.dto.UserLockUpdateRequest;
import com.example.ootd.domain.user.dto.UserRoleUpdateRequest;
import java.util.UUID;
import org.springframework.security.core.Authentication;

public interface AuthService {

  UserDto registerUser(UserCreateRequest request);

  UserDto updateUserRole(UUID userId, UserRoleUpdateRequest request); // 요구사항 따라 Authentication 객체 받을 수도
  void updateUserPassword(Authentication auth, ChangePasswordRequest request);
  UUID changeLockStatus(Authentication auth, UserLockUpdateRequest request);

  void resetPassword(ResetPasswordRequest request);

}
