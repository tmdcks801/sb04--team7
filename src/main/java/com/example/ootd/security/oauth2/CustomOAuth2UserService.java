package com.example.ootd.security.oauth2;

import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.OotdException;
import com.example.ootd.security.Provider;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);

    String registrationId = userRequest.getClientRegistration().getRegistrationId();

    String email;
    String name;
    String providerId;

    if ("google".equals(registrationId)) {
      email = oAuth2User.getAttribute("email");
      name = oAuth2User.getAttribute("name");
      providerId = oAuth2User.getAttribute("sub");
    } else {
      throw new OotdException(ErrorCode.USER_NOT_FOUND);
    }

    User user = userRepository.findByEmail(email)
        .orElseGet(() -> {
          User newUser = new User(name, email, providerId, Provider.GOOGLE);
          return userRepository.save(newUser);
        });

    return new DefaultOAuth2User(
        Collections.singleton(() -> user.getRole().name()),
        oAuth2User.getAttributes(),
        "email"
    );
  }
}
