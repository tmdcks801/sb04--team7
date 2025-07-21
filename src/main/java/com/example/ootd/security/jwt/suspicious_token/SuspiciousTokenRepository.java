package com.example.ootd.security.jwt.suspicious_token;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuspiciousTokenRepository extends JpaRepository<SuspiciousToken, String> {

}
