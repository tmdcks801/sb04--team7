package com.example.ootd.domain.user.service;

import com.example.ootd.domain.user.Gender;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.UserRole;
import com.example.ootd.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminInitializationService implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder encoder;

  @Override
  public void run(String... args) throws Exception {
    Optional<User> existing = userRepository.findByEmail("admin@email.com");

    if (existing.isPresent()) {
      return;
    }

    User user = new User("admin", "admin@email.com", encoder.encode("admin123"), UserRole.ROLE_ADMIN, false, null, null, Gender.OTHER, LocalDate.EPOCH, 3, false, null);

    userRepository.save(user);
  }
}
