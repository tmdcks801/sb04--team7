package com.example.ootd.security.jwt.blacklist;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlackListFlushScheduler {

  private final StringRedisTemplate redisTemplate;
  private final AutoBlackList blackList;

  @Scheduled(fixedDelay = 10000)
  public void recover(){

    if (isRedisAlive()){
      log.debug("Redis alive. Attempting to flush to redis...");
      flushToRedis();
    } else {
      log.warn("Redis is not alive. Skipping flush");
    }
  }

  private boolean isRedisAlive(){
    try (var connection = redisTemplate.getConnectionFactory().getConnection()){
      String pong = connection.ping();
      return "PONG".equalsIgnoreCase(pong);
    } catch (Exception e){
      log.warn("Redis ping failed");
      return false;
    }
  }

  private void flushToRedis(){
    blackList.flushInMemoryToRedis();
  }
}
