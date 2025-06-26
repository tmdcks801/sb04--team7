package com.example.ootd.domain.notification.controller;

import com.example.ootd.domain.notification.dto.NotificationRequest;
import com.example.ootd.domain.notification.service.inter.NotificationPublisherInterface;
import com.example.ootd.domain.notification.service.inter.NotificationServiceInterface;
import com.example.ootd.dto.PageResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
      @RequestParam UUID receiverId, //이거 보니 토큰에 있어서 따로 떼오는거 추가하기
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, defaultValue = "20") int limit) {

    return ResponseEntity.ok(notificationService.getPageNation(receiverId, cursor, limit));
  }

  private final NotificationPublisherInterface notificationPublisherInterface;

  @PostMapping//테스트용
  public ResponseEntity<Void> publish(@RequestBody @Validated NotificationRequest req) {

    notificationPublisherInterface.publish(req);
    return ResponseEntity.accepted().build();
  }


}
