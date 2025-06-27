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
import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.OotdException;
import com.example.ootd.security.jwt.JwtService;
import com.example.ootd.security.jwt.JwtSession;
import com.example.ootd.security.jwt.JwtSessionRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{


  @Value("${app.jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;
  private final UserMapper userMapper;
  private final UserRepository userRepository;
  private final JwtService jwtService;

  private final JwtSessionRepository jwtSessionRepository;

  @Override
  public UserDto registerUser(UserCreateRequest request) {
    User newUser = userMapper.toEntity(request);
    return userMapper.toDto(userRepository.save(newUser));
  }

  @Override
  public String refreshToken(HttpServletRequest req, HttpServletResponse res){

    String refreshToken = extractRefreshTokenFromCookie(req);
    JwtSession newSession = jwtService.rotateRefreshToken(refreshToken);
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
  public void resetPassword(ResetPasswordRequest request) {

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
