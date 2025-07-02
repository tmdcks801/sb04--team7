package com.example.ootd.domain.notification.controller;

import com.example.ootd.domain.notification.service.inter.NotificationServiceInterface;
import com.example.ootd.dto.PageResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationServiceInterface notificationService;

  @GetMapping//애도 테스트 용
  public ResponseEntity<PageResponse> getNotifications(
      @AuthenticationPrincipal Jwt jwt, //이거 보니 토큰에 있어서 따로 떼오는거로 나중에 변경
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, defaultValue = "20") int limit) {

    UUID receiverId = UUID.fromString(jwt.getClaimAsString("userId"));
    return ResponseEntity.ok(notificationService.getPageNation(receiverId, cursor, limit));
  }

  @DeleteMapping("/{notificationId}")
  public ResponseEntity<Void> readNotification(@PathVariable String notificationId) {

    notificationService.readNotification(UUID.fromString(notificationId));
    return ResponseEntity.noContent().build();
  }

//  private final NotificationPublisherInterface notificationPublisherInterface;
//
//  @PostMapping//테스트용
//  public ResponseEntity<Void> publish(@RequestBody @Validated NotificationRequest req) {
//
//    notificationPublisherInterface.publish(req);
//    return ResponseEntity.accepted().build();
//  }


}
