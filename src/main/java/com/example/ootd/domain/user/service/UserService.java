package com.example.ootd.domain.user.service;

import com.example.ootd.domain.user.dto.ChangePasswordRequest;
import com.example.ootd.domain.user.dto.ProfileDto;
import com.example.ootd.domain.user.dto.ProfileUpdateRequest;
import com.example.ootd.domain.user.dto.UserDto;
import com.example.ootd.domain.user.dto.UserLockUpdateRequest;
import com.example.ootd.domain.user.dto.UserPagedResponse;
import com.example.ootd.domain.user.dto.UserRoleUpdateRequest;
import com.example.ootd.domain.user.dto.UserSearchCondition;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

  UserPagedResponse getUsers(UserSearchCondition condition);

  ProfileDto getUserProfile(UUID userId);

  ProfileDto updateUserProfile(UUID userId, ProfileUpdateRequest request, MultipartFile profile);

  UserDto changeUserRole(UserRoleUpdateRequest request, UUID userId);

  @Transactional
  void updateUserPassword(UUID userId, ChangePasswordRequest request);

  @Transactional
  void updateUserLockStatus(UUID userId, UserLockUpdateRequest request);
}
