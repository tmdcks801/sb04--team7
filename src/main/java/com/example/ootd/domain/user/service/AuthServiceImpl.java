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
import com.example.ootd.domain.user.util.EmailService;
import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.OotdException;
import com.example.ootd.security.jwt.JwtService;
import com.example.ootd.security.jwt.JwtSession;
import com.example.ootd.security.jwt.JwtSessionRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{


  @Value("${app.jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;
  private final UserMapper userMapper;
  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final PasswordEncoder encoder;
  private final JwtSessionRepository jwtSessionRepository;
  private final EmailService emailService;
  @Override
  public UserDto registerUser(UserCreateRequest request) {
    User newUser = userMapper.toEntity(request, encoder);
    return userMapper.toDto(userRepository.save(newUser));
  }

  @Override
  public String refreshToken(HttpServletRequest req, HttpServletResponse res){

    String refreshToken = extractRefreshTokenFromCookie(req);

    JwtSession newSession = jwtService.validateAndRotateRefreshToken(refreshToken);
    Cookie newCookie = new Cookie("refresh_token", newSession.getRefreshToken());

    newCookie.setHttpOnly(true);
    newCookie.setPath("/");
    newCookie.setMaxAge((int) refreshTokenExpiration);
    res.addCookie(newCookie);

    return newSession.getAccessToken();
  }

  @Override
  public void signOut(HttpServletRequest request, HttpServletResponse response){
    String refreshToken = extractRefreshTokenFromCookie(request);

    jwtService.invalidateToken(refreshToken);

    Cookie cookie = new Cookie("refresh_token", null);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    cookie.setHttpOnly(true);
    response.addCookie(cookie);

    SecurityContextHolder.clearContext();
  }

  @Override
  public String getAccessToken(HttpServletRequest req){

    String refreshToken = extractRefreshTokenFromCookie(req);

    JwtSession session = jwtSessionRepository.findByRefreshToken(refreshToken)
        .orElseThrow(() -> new OotdException(ErrorCode.AUTHENTICATION_FAILED));

    return session.getAccessToken();
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
  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new OotdException(ErrorCode.USER_NOT_FOUND) );

    String tmpPwd = UUID.randomUUID().toString().substring(0,8);
    String encodedTmpPwd = encoder.encode(tmpPwd);

    user.resetPassword(encodedTmpPwd);
    userRepository.save(user);
    jwtSessionRepository.deleteAllByUser_Id(user.getId());

    sendEmailAsync(user.getEmail(), tmpPwd)
        .whenComplete((result, ex) -> {
          if (ex == null) {
            log.info("[이메일 전송 성공] : email={}", user.getEmail());
          } else {
            log.error("[이메일 전송 실패] : email={}", user.getEmail(), ex);
          }
        });
  }

  @Async // @Async + @Retryable 은 AOP 기반 동작시 에러 -> wrapping 메서드 구현
  public CompletableFuture<Void> sendEmailAsync(String email, String tmpPwd) {
    return CompletableFuture.runAsync(() -> {
      emailService.sendEmail(email, tmpPwd);
    });
  }

  private String extractRefreshTokenFromCookie(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return null;
    }

    for (Cookie cookie : request.getCookies()) {
      if ("refresh_token".equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}
