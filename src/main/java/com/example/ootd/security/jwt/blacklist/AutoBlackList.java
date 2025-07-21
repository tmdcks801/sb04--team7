package com.example.ootd.security.jwt.blacklist;

import com.example.ootd.security.jwt.suspicious_token.SuspiciousToken;
import com.example.ootd.security.jwt.suspicious_token.SuspiciousTokenRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutoBlackList implements BlackList {

  private final InMemoryBlackList memoryBlackList;
  private final RedisBlacklist redisBlacklist;
  private final SuspiciousTokenRepository suspiciousTokenRepository;


  @Override
  @CircuitBreaker(name = "redisBlacklist", fallbackMethod = "addToBlacklistFallback")
  public void addToBlacklist(String jti, Instant expirationTime) {
    redisBlacklist.addToBlacklist(jti, expirationTime);
  }

  @Override
  @CircuitBreaker(name = "redisBlacklist", fallbackMethod = "isBlacklistedFallback")
  public boolean isBlacklisted(String jti) {
    return redisBlacklist.isBlacklisted(jti);
  }

  public void addToBlacklistFallback(String jti, Instant expirationTime, Throwable t){
    log.warn("Fallback to in-memory: {}", t.getMessage());
    memoryBlackList.addToBlacklist(jti, expirationTime);
  }

  public boolean isBlacklistedFallback(String jti, Throwable t){
    log.warn("Fallback to in-memory for isBlacklisted: {}", t.getMessage());
    return memoryBlackList.isBlacklisted(jti);
  }

  public void flushInMemoryToRedis(){
    Map<String, Instant> map = memoryBlackList.getBlacklist();

    if(map.isEmpty()) {
      log.debug("Nothing to flush... Returning");
      return;
    }

    map.forEach((t, ex) -> {
      try {
        redisBlacklist.addToBlacklist(t, ex);
        memoryBlackList.remove(t);
        log.info("Flushed token {}", t);
      } catch (Exception e){
        log.warn("Flush failed. reason: {}", e.getMessage());
      }
    });
  }

  public void flushSuspiciousToRedis(){
    List<SuspiciousToken> tokenList = suspiciousTokenRepository.findAll();

    tokenList
        .forEach(token -> {
          redisBlacklist.addToBlacklist(token.getJti(), token.getExpirationTime());
        });
  }
}
