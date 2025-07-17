package com.example.ootd.config;

import com.example.ootd.security.CustomUserDetailsService;
import com.example.ootd.security.CustomUsernamePasswordAuthenticationFilter;
import com.example.ootd.security.jwt.JwtAuthenticationFilter;
import com.example.ootd.security.oauth2.CustomOAuth2UserService;
import com.example.ootd.security.oauth2.OAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final Environment environment;

  /**
   * 기초 세팅을 위한 임시 필터체인
   */
  @Bean
  public SecurityFilterChain chain(HttpSecurity http,
      AuthenticationManager manager,
      CustomUsernamePasswordAuthenticationFilter customFilter,
      JwtAuthenticationFilter jwtFilter,
      OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
      CustomOAuth2UserService customOAuth2UserService) throws Exception {
    customFilter.setAuthenticationManager(manager);

    http
        .exceptionHandling(e -> e
            .authenticationEntryPoint((request, response, authException) -> {
              response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            })
        )
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> {
          auth.requestMatchers(
                  "/",
                  "/assets/**",
                  "/static/**",
                  "/favicon.ico",
                  "/closet-hanger-logo.png",
                  "/index.html",
                  "/vite.svg",
                  "/actuator/health",
                  "/actuator/prometheus"
              ).permitAll()
              .requestMatchers("/oauth2/callback").permitAll()
              .requestMatchers("/api/auth/me").permitAll()
              .requestMatchers("/api/auth/sign-out").permitAll()
              .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
              .requestMatchers(HttpMethod.PATCH, "/api/users/*/role").hasRole("ADMIN")
              .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
              .requestMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()
              .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
              .requestMatchers("/test/me").hasRole("USER")
              .requestMatchers(HttpMethod.POST, "/api/clothes/attribute-defs").hasRole("ADMIN")
              .requestMatchers(HttpMethod.DELETE, "/api/clothes/attribute-defs/**").hasRole("ADMIN")
              .requestMatchers(HttpMethod.PATCH, "/api/clothes/attribute-defs/**").hasRole("ADMIN")
              .requestMatchers("/sub").permitAll()
              .requestMatchers("/pub").permitAll()
              .requestMatchers("/ws/**").permitAll()
              .requestMatchers("/api/batch/weather/**").hasRole("ADMIN");

          // dev 프로파일에서만 Swagger 허용
          if (java.util.Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            auth.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                .permitAll();
          }
//          auth.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**")
//              .permitAll();

          auth.anyRequest().authenticated();
        })
        .oauth2Login(oauth2 -> oauth2.userInfoEndpoint(userInfo ->
                userInfo.userService(customOAuth2UserService))
            .successHandler(oAuth2LoginSuccessHandler)
            .failureHandler((request, response, exception) -> {
              // 1) 로그
              log.error("OAuth2 로그인 실패 원인:", exception);
            }))
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAt(customFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public AuthenticationManager authManager(HttpSecurity http, PasswordEncoder passwordEncoder,
      CustomUserDetailsService userDetailsService)
      throws Exception {

    AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
    builder
        .userDetailsService(userDetailsService)
        .passwordEncoder(passwordEncoder);

    return builder.build();
  }

  @Bean
  public SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public RoleHierarchy roleHierarchy() {
    String hierarchyString = """
        ROLE_ADMIN > ROLE_USER
        """;

    return RoleHierarchyImpl.fromHierarchy(hierarchyString);
  }

//  @Bean
//  public WebSecurityCustomizer webSecurityCustomizer() {
//    return (web) -> web.ignoring()
//        .requestMatchers(
//            "/api/notifications"         //알림 테스트용
//        );
//  }


}
