package com.example.ootd.security.jwt.blacklist;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryBlackList implements BlackList{

  private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

  @Override
  public void addToBlacklist(String jti, Instant expirationTime) {
    blacklist.put(jti, expirationTime);
  }

  @Override
  public boolean isBlacklisted(String jti) {
    Instant expirationTime = blacklist.get(jti);

    if (expirationTime == null) {
      return false;
    }
    if (expirationTime.isBefore(Instant.now())) {
      blacklist.remove(jti);
      return false;
    }

    return true;
  }

  public Map<String, Instant> getBlacklist() {
    return blacklist; // unmodifiable?
  }

  public void remove(String jti){
    blacklist.remove(jti);
  }
}
