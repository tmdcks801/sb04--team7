package com.example.ootd.security.jwt.suspicious_token;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SuspiciousToken {

  @Id
  @Column(name = "jti", nullable = false, updatable = false)
  private String jti;

  @Column(nullable = false)
  private Instant expirationTime;

  public SuspiciousToken(String jti, Instant expirationTime) {
    this.jti = jti;
    this.expirationTime = expirationTime;
  }
}
