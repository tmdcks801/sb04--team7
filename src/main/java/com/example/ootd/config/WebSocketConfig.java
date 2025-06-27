package com.example.ootd.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker         // <- 핵심!
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  /**
   * 메시지 브로커 경로 설정 (구독용)
   */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/sub");     // 클라이언트가 구독하는 prefix
    registry.setApplicationDestinationPrefixes("/pub"); // 서버로 보내는 prefix
  }

  /**
   * 웹소켓 엔드포인트 등록 (핸드셰이크)
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")             // → ws://{host}/ws
        .setAllowedOrigins("*")         // CORS
        .withSockJS();                  // SockJS fallback (선택)
  }
}