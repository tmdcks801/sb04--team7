package com.example.ootd.security.jwt.blacklist;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreaker.StateTransition;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisRecoveryConfig {

  private final CircuitBreakerRegistry circuitBreakerRegistry;
  private final AutoBlackList autoBlackList;

  @PostConstruct
  public void registerListener(){
    log.info("LISTENING...");
    CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("redisBlacklist");

    cb.getEventPublisher()
        .onStateTransition(event -> {

          log.info("CB Event: {} → {}", event.getStateTransition().getFromState(), event.getStateTransition().getToState());


          // 테스트 및 검증용
          if (event.getStateTransition() == StateTransition.OPEN_TO_HALF_OPEN) {

            log.info("[CB OPEN → HALF_OPEN] triggering test call...");

            try {
              autoBlackList.addToBlacklist("probe-" + UUID.randomUUID(), Instant.now().plusSeconds(30));
            } catch (Exception e) {
              log.warn("Probe call failed: {}", e.getMessage());
            }
          }


          if (event.getStateTransition() == StateTransition.HALF_OPEN_TO_CLOSED) {
            log.info("[CB HALF_OPEN → CLOSED] Redis recovered. Flushing blacklist.");
            autoBlackList.flushInMemoryToRedis();
          }
        });
  }

}
