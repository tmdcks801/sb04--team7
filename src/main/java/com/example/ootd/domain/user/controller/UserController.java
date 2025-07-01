package com.example.ootd.domain.user.controller;


import com.example.ootd.domain.user.dto.ProfileDto;
import com.example.ootd.domain.user.dto.UserDto;
import com.example.ootd.domain.user.dto.UserPagedResponse;
import com.example.ootd.domain.user.dto.UserRoleUpdateRequest;
import com.example.ootd.domain.user.dto.UserSearchCondition;
import com.example.ootd.domain.user.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
