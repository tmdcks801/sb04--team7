package com.example.ootd.security.jwt.blacklist;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


// 토큰 재발급 / 비정상적 요청시 블렉리스트 등록
// 추후 redis 사용 예정
public interface BlackList {
  // private static final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

  public void addToBlacklist(String accessToken, Instant expirationTime) ;
//  {
//    blacklist.put(accessToken, expirationTime);
//  }

  public boolean isBlacklisted(String accessToken) ;
//  {
//    Instant expirationTime = blacklist.get(accessToken);
//
//    if (expirationTime == null) {
//      return false;
//    }
//    if (expirationTime.isBefore(Instant.now())) {
//      blacklist.remove(accessToken);
//      return false;
//    }
//
//    return true;
//  }
}
