package com.example.ootd.domain.notification.controller;

import com.example.ootd.domain.notification.dto.NotificationRequest;
import com.example.ootd.domain.notification.service.inter.NotificationPublisherInterface;
import com.example.ootd.domain.notification.service.inter.NotificationServiceInterface;
import com.example.ootd.domain.sse.service.SsePushServiceInterface;
import com.example.ootd.dto.PageResponse;
import jakarta.annotation.security.PermitAll;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationServiceInterface notificationService;

  @GetMapping
  public ResponseEntity<PageResponse> getNotifications(
      @AuthenticationPrincipal(expression = "user.id") UUID receiverId,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, defaultValue = "20") int limit) {
    if (receiverId == null) {                       // 안전장치

      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 로그인 정보");
    }
    return ResponseEntity.ok(notificationService.getPageNation(receiverId, cursor, limit));
  }

  @DeleteMapping("/{notificationId}")
  public ResponseEntity<Void> readNotification(@PathVariable String notificationId) {

    notificationService.readNotification(UUID.fromString(notificationId));
    return ResponseEntity.noContent().build();
  }

//  private final NotificationPublisherInterface notificationPublisherInterface;
//  private final SsePushServiceInterface ssePushServiceInterface;
//
//  @PostMapping//테스트용
//  @PermitAll
//  public ResponseEntity<Void> publish(@RequestBody @Validated NotificationRequest req) {
//
//    ssePushServiceInterface.subscribe(req.receiverId(), null);
//    notificationPublisherInterface.publish(req);
//    return ResponseEntity.accepted().build();
//  }


}
