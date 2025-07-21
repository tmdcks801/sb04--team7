package com.example.ootd.security.jwt.blacklist;


import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisBlacklist implements BlackList {

  private final StringRedisTemplate redisTemplate;
  private static final String BLACKLIST_KEY_PREFIX = "blacklist:jti:";
  @Override
  public void addToBlacklist(String jti, Instant expirationTime) {
    long ttl = Duration.between(Instant.now(), expirationTime).getSeconds();
    if (ttl > 0) {
      redisTemplate.opsForValue().set(BLACKLIST_KEY_PREFIX + jti, "blacklisted", Duration.ofSeconds(ttl));
    }
  }

  @Override
  public boolean isBlacklisted(String jti) {
    Boolean exists = redisTemplate.hasKey(BLACKLIST_KEY_PREFIX + jti);
    return Boolean.TRUE.equals(exists);
  }
}
