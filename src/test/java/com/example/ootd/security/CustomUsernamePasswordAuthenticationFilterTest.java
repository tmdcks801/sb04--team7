package com.example.ootd.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.example.ootd.TestEntityFactory;
import com.example.ootd.domain.sse.service.SsePushServiceInterface;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.dto.LoginDto;
import com.example.ootd.exception.OotdException;
import com.example.ootd.security.jwt.JwtService;
import com.example.ootd.security.jwt.JwtSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class CustomUsernamePasswordAuthenticationFilterTest {

  @InjectMocks
  CustomUsernamePasswordAuthenticationFilter filter;
  @Mock
  SsePushServiceInterface ssePushServiceInterface;
  @Mock
  JwtService jwtService;
  @Mock
  AuthenticationManager manager;

  MockHttpServletResponse response;
  MockHttpServletRequest request;
  @Mock
  FilterChain filterChain;

  private final String TEST_EMAIL = "test@gmail.com";
  private final String TEST_PASSWORD = "test-password";
  private final UUID TEST_USER_ID = UUID.randomUUID();
  private final String TEST_ACCESS_TOKEN = "test.access.token";
  private final String TEST_REFRESH_TOKEN = "test.refresh.token";
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final long TEST_REFRESH_TOKEN_EXPIRATION = 86400000 * 7;


  @BeforeEach
  void setup() throws Exception {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    ReflectionTestUtils.setField(filter, "refreshTokenExpiration", TEST_REFRESH_TOKEN_EXPIRATION);
    filter.setAuthenticationManager(manager);
    filter.setRequiresAuthenticationRequestMatcher(request ->
        request.getRequestURI().equals("/api/auth/sign-in") &&
            request.getMethod().equalsIgnoreCase("POST"));
  }

  @Test
  @DisplayName("인증 시도 성공")
  void 인증_시도_성공() throws JsonProcessingException {
    // given
    LoginDto loginDto = new LoginDto(TEST_EMAIL, TEST_PASSWORD);
    String jsonRequrest = objectMapper.writeValueAsString(loginDto);
    request.setContent(jsonRequrest.getBytes());
    request.setContentType("application/json");
    request.setMethod("POST");
    request.setRequestURI("/api/auth/sign-in");

    UsernamePasswordAuthenticationToken expectedToken = new UsernamePasswordAuthenticationToken(TEST_EMAIL, TEST_PASSWORD);
    Authentication authentication = mock(Authentication.class);
    given(manager.authenticate(eq(expectedToken))).willReturn(authentication);

    // when
    Authentication result = filter.attemptAuthentication(request, response);

    // then
    assertThat(result).isEqualTo(authentication);
    verify(manager, times(1)).authenticate(eq(expectedToken));
  }

  @Test
  @DisplayName("인증 시도 실패")
  void 인증_시도_실패(){
    request.setContent("invalid".getBytes());
    request.setContentType("application/json");
    request.setMethod("POST");
    request.setRequestURI("/api/auth/sign-in");

    assertThatThrownBy(() -> filter.attemptAuthentication(request, response))
        .isInstanceOf(OotdException.class);
  }


  @Nested
  @DisplayName("successfulAuthentication 테스트")
  class SuccessfulAuthenticationTests{

    private CustomUserDetails userDetails;
    private User user;
    private Authentication authentication;
    private JwtSession session;

    @BeforeEach
    void setup(){
      request = new MockHttpServletRequest();
      response = new MockHttpServletResponse();

      user = TestEntityFactory.createUser();
      userDetails = new CustomUserDetails(user);
      authentication = mock(Authentication.class);
      session = new JwtSession(user, TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN);

      SecurityContext context = SecurityContextHolder.createEmptyContext();
      SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("successfulAuthentication 성공")
    void successfulAuthentication_success() throws ServletException, IOException {
      // given
      given(authentication.getPrincipal()).willReturn(userDetails);
      given(jwtService.generateJwtSession(user)).willReturn(session);
      // when
      filter.successfulAuthentication(request, response, filterChain, authentication);

      // then
      Cookie cookie = response.getCookie("refresh_token");
      assertThat(cookie).isNotNull();
      assertThat(cookie.getValue()).isEqualTo(TEST_REFRESH_TOKEN);
    }
  }


//  @Test
//  @DisplayName("unsuccessfulAuthentication 실패")
//  void unsuccessfulAuthentication_returnsErrorResponse(){
//    //
//    AuthenticationException exception = new AuthenticationException("test reason") {};
//    assertThatThrownBy(() -> filter.unsuccessfulAuthentication(request, response, exception)).isInstanceOf(OotdException.class);
//
//    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
//  }
}
