package com.example.ootd.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  UserRepository userRepository;

  @Autowired
  PasswordEncoder encoder;

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
    @Transactional
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
  }
}
