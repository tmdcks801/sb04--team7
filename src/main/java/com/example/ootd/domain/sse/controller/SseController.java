package com.example.ootd.domain.sse.controller;

import com.example.ootd.domain.sse.service.SsePushServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController {

  private final SsePushServiceInterface ssePushService;

//  @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//  public SseEmitter subscribe(
//      @RequestParam(value = "LastEventId", required = false)
//      UUID lastEventId,
//
//      @AuthenticationPrincipal CustomUserPrincipal principal,
//      HttpServletResponse response) {
//
//    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, max-age=0, must-revalidate");
//    response.setHeader(HttpHeaders.PRAGMA, "no-cache");
//    response.setHeader(HttpHeaders.EXPIRES, "0");
//    response.setHeader("X-Content-Type-Options", "nosniff");
//    response.setHeader("X-Frame-Options", "DENY");
//    response.setHeader("X-XSS-Protection", "0");
//
//    response.setHeader("X-Accel-Buffering", "no");
//
//    UUID userId = principal.getUserId();
//    return ssePushService.subscribe(userId, lastEventId);
//  }

}