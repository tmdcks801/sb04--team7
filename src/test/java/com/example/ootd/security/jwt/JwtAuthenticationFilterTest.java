package com.example.ootd.security.jwt;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.example.ootd.security.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

  @InjectMocks
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Mock
  private JwtService jwtService;
  @Mock
  private UserDetailsService userDetailsService;
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private FilterChain filterChain;

  @Test
  @DisplayName("토큰을 정상적으로 파싱할 수 있다")
  void 토큰을_정상적으로_파싱할_수_있다() throws ServletException, IOException {
    // given
    String token = "test.token.jwt";
    String email = "test@gmail.com";
    CustomUserDetails userDetails = mock(CustomUserDetails.class);

    given(request.getHeader("Authorization"))
        .willReturn("Bearer " + token);
    given(jwtService.validateToken(any())).willReturn(true);
    given(jwtService.extractEmail(token)).willReturn(email);
    given(userDetailsService.loadUserByUsername(email)).willReturn(userDetails);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNotNull();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("헤더 포멧이 틀릴경우 예외")
  void 헤더_포멧이_틀릴경우_예외() throws ServletException, IOException {
    // given
    String token = "test.token.jwt";
    given(request.getHeader("Authorization"))
        .willReturn("Wrong " + token);

    // when
    jwtAuthenticationFilter.doFilterInternal(request,response,filterChain);

    // then
    verify(jwtService, never()).extractEmail(any());
    verify(userDetailsService, never()).loadUserByUsername(any());
    Assertions.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
}
