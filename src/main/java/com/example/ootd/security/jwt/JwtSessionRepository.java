package com.example.ootd.security.jwt;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JwtSessionRepository extends JpaRepository<JwtSession, UUID> {
  Optional<JwtSession> findByRefreshToken(String refreshToken);

  Optional<JwtSession> findByUser_Id(UUID userId);

  void deleteAllByUser_Id(UUID userId);

}
