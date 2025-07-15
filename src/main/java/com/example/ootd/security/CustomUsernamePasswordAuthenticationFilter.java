package com.example.ootd.security;

import com.example.ootd.domain.sse.service.SsePushServiceInterface;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.dto.LoginDto;
import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.ErrorResponse;
import com.example.ootd.exception.OotdException;
import com.example.ootd.security.jwt.JwtService;
import com.example.ootd.security.jwt.JwtSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomUsernamePasswordAuthenticationFilter extends
    UsernamePasswordAuthenticationFilter {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final SsePushServiceInterface ssePushServiceInterface;
  private final JwtService jwtService;
  private final AuthenticationManager manager;
  @Value("${app.jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;

  /**
   * 필터 초기 설정 - Authentication Manager 설정 - 경로 설정
   */
  @PostConstruct
  public void init() {
    setAuthenticationManager(manager);
    setRequiresAuthenticationRequestMatcher(request ->
        request.getRequestURI().equals("/api/auth/sign-in") &&
            request.getMethod().equalsIgnoreCase("POST"));
  }

  /**
   * 로그인 요청시 호출됨 - 요청 body 에서 이메일, 비밀번호 파싱 후 AuthenticationManager 에 위임
   */
  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) throws AuthenticationException {
    try {
      LoginDto loginDto = objectMapper.readValue(request.getInputStream(), LoginDto.class);
      UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
          loginDto.email(), loginDto.password());

      return this.getAuthenticationManager().authenticate(token);
    } catch (IOException e) {
      log.info("AUTHENTICATION FAILED : reason={}", e.getMessage());
      throw new OotdException(ErrorCode.AUTHENTICATION_FAILED);
    }
  }

  /**
   * 로그인 성공시 호출 - SecurityContext에 인증 객체 저장 - 세션에도 사용자 정보 저장 - JwtSession 생성
   */
  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain, Authentication authResult) throws IOException, ServletException {
    SecurityContextHolder.getContext().setAuthentication(authResult);

    new HttpSessionSecurityContextRepository().saveContext(SecurityContextHolder.getContext(),
        request, response);

    CustomUserDetails userDetails = (CustomUserDetails) authResult.getPrincipal();
    User user = userDetails.getUser();

//    HttpSession sessionObj = request.getSession(false);
//    if (sessionObj != null) {
//      sessionObj.setAttribute("userId", user.getId());
//      sessionRegistry.registerNewSession(sessionObj.getId(), authResult.getPrincipal());
//    }

    //여기서 sse로직
//    UUID lastEventId = null;
//    String lastIdHeader = request.getHeader("Last-Event-ID");
//    if (StringUtils.hasText(lastIdHeader)) {
//      try {
//        lastEventId = UUID.fromString(lastIdHeader);
//      } catch (IllegalArgumentException ignore) {//일단 무시
//      }
//    }
//    ssePushServiceInterface.subscribe(user.getId(), lastEventId);//여기까지

    JwtSession session = jwtService.generateJwtSession(user);

    Cookie refreshToken = new Cookie("refresh_token", session.getRefreshToken());
    refreshToken.setHttpOnly(true);
    refreshToken.setSecure(true);
    refreshToken.setPath("/");
    refreshToken.setMaxAge((int) (refreshTokenExpiration / 1000));
    response.addCookie(refreshToken);

    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(), session.getAccessToken());
  }

  @Override // TODO : ErrorResponse 구현 후 구현
  protected void unsuccessfulAuthentication(HttpServletRequest request,
      HttpServletResponse response, AuthenticationException failed)
      throws IOException, ServletException {

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    objectMapper.writeValue(response.getWriter(), new ErrorResponse(
        "AuthenticationException",
        "인증에 실패하였습니다.",
        Map.of("reason", failed.getMessage())
    ).toString());

//    throw new OotdException(ErrorCode.AUTHENTICATION_FAILED);
  }
}
