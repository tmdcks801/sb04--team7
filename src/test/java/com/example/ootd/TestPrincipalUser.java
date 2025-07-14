package com.example.ootd;

import com.example.ootd.domain.user.User;
import com.example.ootd.security.PrincipalUser;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;

public class TestPrincipalUser implements PrincipalUser {

  private final User user;

  public TestPrincipalUser(User user) {
    this.user = user;
  }

  @Override
  public User getUser() {
    return user;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // 테스트용 권한, 필요에 따라 변경 가능
    return List.of(() -> "ROLE_USER");
  }

  @Override
  public boolean isOAuthUser() {
    return false;
  }
}
