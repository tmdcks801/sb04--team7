package com.example.ootd.security.oauth2;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

import com.example.ootd.domain.user.Gender;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.UserRole;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.security.Provider;
import com.example.ootd.security.oauth2.CustomOAuth2UserService.OAuth2Provider;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

@ExtendWith(MockitoExtension.class)
public class CustomOAuth2UserServiceTest {

  @InjectMocks
  CustomOAuth2UserService customOAuth2UserService;

  @Mock
  UserRepository userRepository;

  @Test
  @DisplayName("구글 OAUTH 신규 성공")
  void google_oAuth_new_user_success(){
    // given
    Map<String, Object> googleAttributes = Map.of(
        "email", "test@gmail.com",
        "name", "홍길동",
        "sub", "providerId"
    );
    when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());
    User newUser = new User("홍길동", "test@gmail.com", "providerId", Provider.GOOGLE);
    when(userRepository.save(any(User.class))).thenReturn(newUser);

    // when
    User result = CustomOAuth2UserService.OAuth2Provider.GOOGLE.getOrCreateUser(googleAttributes, userRepository);

    // then
    assertThat(result).isEqualTo(newUser);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  @DisplayName("구글 OAUTH 기존 성공")
  void google_oAuth_existing_user_success(){
    Map<String, Object> googleAttributes = Map.of(
        "email", "test@gmail.com",
        "name", "홍길동",
        "sub", "providerId"
    );
    User user = new User("홍길동", "test@gmail.com", "providerId", Provider.GOOGLE);
    when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));

    // when
    User result = CustomOAuth2UserService.OAuth2Provider.GOOGLE.getOrCreateUser(googleAttributes, userRepository);

    // then
    assertThat(result).isEqualTo(user);
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("카카오 OAUTH 신규 성공")
  void kakao_oAuth_new_user_success(){
    Map<String, Object> kakaoProperties = Map.of("nickname", "카카오닉네임");
    Map<String, Object> kakaoAttributes = Map.of(
        "id", 1L,
        "properties", kakaoProperties
    );
    given(userRepository.findByEmail(any())).willReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    // when
    User result = OAuth2Provider.KAKAO.getOrCreateUser(kakaoAttributes, userRepository);

    // then

    assertThat(result.getEmail()).isEqualTo("카카오닉네임@kakao.com");
  }
}
