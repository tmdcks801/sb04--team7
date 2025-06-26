package com.example.ootd.domain.user;


import com.example.ootd.domain.user.dto.UserCreateRequest;
import com.example.ootd.domain.user.dto.UserDto;
import com.example.ootd.domain.user.service.AuthService;
import com.example.ootd.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/users")
  public ResponseEntity<UserDto> registerUser(@RequestBody UserCreateRequest request){
    UserDto dto = authService.registerUser(request);
    return ResponseEntity.ok(dto);
  }
}
