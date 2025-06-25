package com.example.ootd.domain.notification.controller;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.dto.NotificationRequest;
import com.example.ootd.domain.notification.service.NotificationPublisherInterface;
import java.time.Instant;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

  //private final NotificationPublisherInterface notificationPublisherInterface;

//  @PostMapping//테스트용
//  public ResponseEntity<Void> publish(@RequestBody @Validated NotificationRequest req) {
//
//    notificationPublisherInterface.publish(req);
//    return ResponseEntity.accepted().build();
//  }


}
