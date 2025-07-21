package com.example.ootd.security.jwt;

import com.example.ootd.security.jwt.blacklist.AutoBlackList;
import com.example.ootd.security.jwt.suspicious_token.SuspiciousToken;
import com.example.ootd.security.jwt.suspicious_token.SuspiciousTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenValidator {

  private final SuspiciousTokenRepository suspiciousTokenRepository;
  private final AutoBlackList autoBlackList;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean validate(String token, String jti, Key signingKey, Instant expiration){
    try {

      if (autoBlackList.isBlacklisted(jti)) {
        log.warn("블랙리스트에 등록된 토큰입니다. TOKEN : {}", token);


        boolean exists = suspiciousTokenRepository.findById(jti).isPresent();

        if(!exists) {
          suspiciousTokenRepository.save(new SuspiciousToken(jti, expiration));
        }

        return false;
      }

      Jwts.parserBuilder()
          .setSigningKey(signingKey)
          .build()
          .parseClaimsJws(token);

      return true;
    } catch (ExpiredJwtException e) {
      log.warn("토큰이 만료되었습니다: {}", e.getMessage());
      return false;
    } catch (JwtException e) {
      log.warn("유효하지 않은 토큰입니다: {}", e.getMessage());
      return false;
    }
  }
}
