package com.example.ootd.domain.follow.controller;


import com.example.ootd.domain.follow.dto.FollowCreateRequest;
import com.example.ootd.domain.follow.dto.FollowDto;
import com.example.ootd.domain.follow.dto.FollowSummaryDto;
import com.example.ootd.domain.follow.service.FollowService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/follows")
@Slf4j
public class FollowController {

  private final FollowService followService;

  @PostMapping
  public ResponseEntity<FollowDto> createFollow(@Valid @RequestBody FollowCreateRequest request) {
      log.info("팔로우 생성 요청 : {}", request);
      FollowDto follow = followService.createFollow(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(follow);
  }

  @GetMapping("/summary")
  public ResponseEntity<FollowSummaryDto> getSummary(@RequestBody UUID userId) {
    log.info("팔로우 요약 정보 요청 : userId = {}", userId);
    FollowSummaryDto summary = followService.getSummaryFollow(userId);
    return ResponseEntity.ok().body(summary);
  }

  @DeleteMapping("/{followId}")
  public ResponseEntity<Void> deleteFollow(@PathVariable UUID followId) {
    log.info("팔로우 취소 요청 : followId = {}", followId);
    followService.deleteFollow(followId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

}
