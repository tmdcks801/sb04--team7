package com.example.ootd.security;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.security.jwt.BlackList;
import com.example.ootd.security.jwt.JwtSessionRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import java.io.UnsupportedEncodingException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
@ActiveProfiles("test")
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  UserRepository userRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtSessionRepository jwtSessionRepository;
  @Autowired
  EntityManager em;
  @Nested
  @DisplayName("폼 로그인 테스트")
  class FormLoginTest{

    User user;

    @BeforeEach
    void setUp(){
       user = userRepository.save(
          new User("test", "test@gmail.com", encoder.encode("test"))
      );
    }

    @Test
    @DisplayName("로그인 성공 후 권한 필요 API 호출")
    void 로그인_성공_후_엔드포인트_호출() throws Exception {
      String json = """
          { "email" : "test@gmail.com", "password" : "test" }
          """;

      MvcResult loginResult = mockMvc.perform(post("/api/auth/sign-in")
          .contentType("application/json")
          .content(json))
          .andExpect(status().isOk())
          .andReturn();

      String accessToken = loginResult.getResponse().getContentAsString();
      if (accessToken.startsWith("\"") && accessToken.endsWith("\"")) {
        accessToken = accessToken.substring(1, accessToken.length() - 1);
      }
      String userId = user.getId().toString();

      mockMvc.perform(get("/api/users/" + userId + "/profiles")
              .header("Authorization", "Bearer " + accessToken))
          .andExpect(status().isOk())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value(user.getName()));
    }

    @Test
    @DisplayName("틀린 비밀번호 로그인 실패")
    void 틀린_비밀번호_로그인_실패() throws Exception {
      String json = """
          { "email" : "test@gmail.com", "password" : "wrong" }
          """;

      MvcResult loginResult = mockMvc.perform(post("/api/auth/sign-in")
              .contentType("application/json")
              .content(json))
          .andExpect(status().isUnauthorized())
          .andReturn();
    }

    @Test
    @DisplayName("비밀번호 초기화시 기존 비밀번호 실패")
    void 비밀번호_초기화시_기존_비밀번호_실패() throws Exception {

      String json = """
          { "email" : "test@gmail.com" }
          """;
      mockMvc.perform(post("/api/auth/reset-password")
          .contentType("application/json")
          .content(json))
          .andExpect(status().isOk())
          .andReturn();

      String json2 = """
          { "email" : "test@gmail.com", "password" : "test" }
          """;

      mockMvc.perform(post("/api/auth/sign-in")
              .contentType("application/json")
              .content(json2)
          )
          .andExpect(status().isUnauthorized())
          .andReturn();
    }

    @Test
    @DisplayName("로그아웃시 세션 정보 삭제")
    void 로그아웃시_세션_정보_삭제() throws Exception {
      String json = """
          { "email" : "test@gmail.com", "password" : "test" }
          """;

      MvcResult loginResult = mockMvc.perform(post("/api/auth/sign-in")
              .contentType("application/json")
              .content(json))
          .andExpect(status().isOk())
          .andReturn();

      int cnt = jwtSessionRepository.findAll().size();

      assertThat(cnt).isEqualTo(1);

      String accessToken = loginResult
          .getResponse()
          .getContentAsString();

      String setCookie = loginResult
          .getResponse()
          .getHeader("Set-Cookie");

      if (accessToken.startsWith("\"") && accessToken.endsWith("\"")) {
        accessToken = accessToken.substring(1, accessToken.length() - 1);
      }

      String refreshTokenCookie = setCookie
          .split(";", 2)[0]
          .replace("refresh_token=", "");

      mockMvc.perform(post("/api/auth/sign-out")
              .header("Authorization", "Bearer " + accessToken)
              .cookie(new Cookie("refresh_token", refreshTokenCookie)))
          .andExpect(status().isNoContent())
          .andReturn();

      int cntAfterSignOut = jwtSessionRepository.findAll().size();

      assertThat(cntAfterSignOut)
          .isEqualTo(0);
    }

    @Test
    @DisplayName("엑세스 토큰을 조회할 수 있다")
    void 엑세스_토큰을_조회할_수_있다() throws Exception {
      String json = """
          { "email" : "test@gmail.com", "password" : "test" }
          """;

      MvcResult loginResult = mockMvc.perform(post("/api/auth/sign-in")
              .contentType("application/json")
              .content(json))
          .andExpect(status().isOk())
          .andReturn();

      String setCookie = loginResult
          .getResponse()
          .getHeader("Set-Cookie");

      String accessToken = loginResult
          .getResponse()
          .getContentAsString();

      if (accessToken.startsWith("\"") && accessToken.endsWith("\"")) {
        accessToken = accessToken.substring(1, accessToken.length() - 1);
      }

      String refreshTokenCookie = setCookie
          .split(";", 2)[0]
          .replace("refresh_token=", "");

      MvcResult tokenResult = mockMvc.perform(get("/api/auth/me").cookie(new Cookie("refresh_token", refreshTokenCookie)))
          .andExpect(status().isOk())
          .andReturn();

      String token = tokenResult.getResponse().getContentAsString();

      assertThat(token).isEqualTo(accessToken);
    }

    @Test
    @DisplayName("토큰을 재발급 받을시 기존 토큰은 블랙리스트")
    void 토큰을_재발급_받을시_기존_토큰은_블랙리스트() throws Exception {
      String json = """
          { "email" : "test@gmail.com", "password" : "test" }
          """;

      MvcResult loginResult = mockMvc.perform(post("/api/auth/sign-in")
              .contentType("application/json")
              .content(json))
          .andExpect(status().isOk())
          .andReturn();

      String setCookie = loginResult
          .getResponse()
          .getHeader("Set-Cookie");

      String accessToken = loginResult
          .getResponse()
          .getContentAsString();

      if (accessToken.startsWith("\"") && accessToken.endsWith("\"")) {
        accessToken = accessToken.substring(1, accessToken.length() - 1);
      }

      Thread.sleep(1000);

      String refreshTokenCookie = setCookie
          .split(";", 2)[0]
          .replace("refresh_token=", "");

      MvcResult result = mockMvc.perform(post("/api/auth/refresh")
              .cookie(new Cookie("refresh_token", refreshTokenCookie)))
          .andReturn();

      String newAccessToken = result.getResponse().getContentAsString();
      if (newAccessToken.startsWith("\"") && newAccessToken.endsWith("\"")) {
        newAccessToken = newAccessToken.substring(1, newAccessToken.length() - 1);
      }

      assertThat(newAccessToken).isNotEqualTo(accessToken);
      assertThat(BlackList.isBlacklisted(accessToken)).isTrue();
    }
  }

}
