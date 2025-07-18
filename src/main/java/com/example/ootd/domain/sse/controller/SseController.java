package com.example.ootd.domain.sse.controller;

import com.example.ootd.config.api.SseApi;
import com.example.ootd.domain.sse.service.SsePushServiceInterface;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController implements SseApi {

  private final SsePushServiceInterface ssePushService;

  @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(
      @RequestParam(value = "LastEventId", required = false) UUID lastEventId,
      @AuthenticationPrincipal(expression = "user.id") UUID receiverId) {

    return ssePushService.subscribe(receiverId, lastEventId);
  }

}