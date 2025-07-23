package com.example.ootd.security.oauth2;

import com.example.ootd.domain.sse.service.SsePushServiceInterface;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.security.jwt.JwtService;
import com.example.ootd.security.jwt.JwtSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Member;
import java.net.URLEncoder;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

//여기에 하나
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {


  private final UserRepository userRepository;
  private final JwtService jwtService;
  @Value("${app.jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;
  private final ObjectMapper mapper = new ObjectMapper();
  private final SsePushServiceInterface ssePushServiceInterface;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    new HttpSessionSecurityContextRepository().saveContext(context, request, response);



    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    String email = oAuth2User.getAttribute("email");

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));


    JwtSession session = jwtService.generateJwtSession(user);

    String refreshToken = session.getRefreshToken();
    Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);

    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setHttpOnly(true);
    response.addCookie(refreshTokenCookie);
    response.setStatus(HttpServletResponse.SC_FOUND);
    response.setHeader("Location", "/");
  }
}
