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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
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

  @Override//여기sse구독
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    SecurityContextHolder.getContext().setAuthentication(authentication);

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    String email = oAuth2User.getAttribute("email");

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

//    //여기서 sse로직
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

    String refreshToken = session.getRefreshToken();
    Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);

    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setHttpOnly(true);
    response.addCookie(refreshTokenCookie);
    response.setStatus(HttpServletResponse.SC_FOUND);
    response.setHeader("Location", "/");
  }
}
