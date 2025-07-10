package com.example.ootd.domain.user.controller;


import com.example.ootd.domain.user.dto.ChangePasswordRequest;
import com.example.ootd.domain.user.dto.ProfileDto;
import com.example.ootd.domain.user.dto.ProfileUpdateRequest;
import com.example.ootd.domain.user.dto.UserDto;
import com.example.ootd.domain.user.dto.UserLockUpdateRequest;
import com.example.ootd.domain.user.dto.UserPagedResponse;
import com.example.ootd.domain.user.dto.UserRoleUpdateRequest;
import com.example.ootd.domain.user.dto.UserSearchCondition;
import com.example.ootd.domain.user.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @GetMapping
  public ResponseEntity<UserPagedResponse> getUserList(@ModelAttribute UserSearchCondition condition){
    return ResponseEntity.ok(userService.getUsers(condition));
  }

  @GetMapping("/{userId}/profiles")
  public ResponseEntity<ProfileDto> getUserProfile(@PathVariable UUID userId){
    return ResponseEntity.ok(userService.getUserProfile(userId));
  }

  @PatchMapping("/{userId}/role")
  public ResponseEntity<UserDto> updateUserRole(@PathVariable UUID userId, @RequestBody
      UserRoleUpdateRequest request){
    return ResponseEntity.ok(userService.changeUserRole(request, userId));
  }

  @PatchMapping(
      value = "/{userId}/profiles",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<ProfileDto> updateUserProfile(@PathVariable UUID userId, @RequestPart
      ProfileUpdateRequest request, @RequestPart(required = false) MultipartFile profile){
    return ResponseEntity.ok(userService.updateUserProfile(userId, request, profile));
  }

  @PatchMapping("/{userId}/password")
  public ResponseEntity<Void> updatePassword(@PathVariable UUID userId, @RequestBody
      ChangePasswordRequest request){
    userService.updateUserPassword(userId, request);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{userId}/lock")
  public ResponseEntity<String> changeLockStatus(@PathVariable UUID userId, @RequestBody
      UserLockUpdateRequest request){
    userService.updateUserLockStatus(userId, request);
    return ResponseEntity.ok().build();
  }
}
