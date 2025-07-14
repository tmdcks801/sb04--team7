package com.example.ootd.domain.notification.service.impli;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.dto.NotificationRequest;
import com.example.ootd.domain.notification.entity.Notification;
import com.example.ootd.domain.notification.enums.NotificationLevel;
import com.example.ootd.domain.notification.mapper.NotificationMapper;
import com.example.ootd.domain.notification.repository.NotificationRepository;
import com.example.ootd.domain.notification.service.inter.NotificationServiceInterface;
import com.example.ootd.dto.PageResponse;
import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.notification.FailReadNotification;
import com.example.ootd.exception.notification.NotFoundNotification;
import com.example.ootd.exception.notification.NotificationCreateError;
import com.example.ootd.exception.notification.NotificationPaginationError;
import com.mongodb.bulk.BulkWriteError;
import com.mongodb.bulk.BulkWriteResult;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.BulkOperationException;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@CacheConfig(cacheNames = "notification")
public class NotificationServiceImpl implements NotificationServiceInterface {

  private final NotificationRepository repository;
  private final MongoTemplate mongoTemplate;
  private final NotificationMapper notificationMapper;

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @CachePut(key = "#result.id")
  public NotificationDto createNotification(NotificationDto dto) {

    try {
      Notification notification = Notification.createNotification(dto);
      repository.save(notification);
      return notificationMapper.toDto(notification);
    } catch (Exception e) {
      log.warn("알림 오류", e);
      throw new NotificationCreateError(
          ErrorCode.FAIL_CREATE_NOTIFICATION);
    }
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @CachePut(key = "#result.id")
  public NotificationDto createNotification(NotificationRequest req) {
    try {
      Notification notification = Notification.createNotification(req);
      repository.save(notification);
      return notificationMapper.toDto(notification);
    } catch (NotificationCreateError e) {
      log.warn("알림 오류", e);
      throw new NotificationCreateError(
          ErrorCode.FAIL_CREATE_NOTIFICATION);
    }

  }

  @Override
  @Transactional(readOnly = true)
  public NotificationDto get(UUID notificationId) {
    try {
      Notification notification = repository.findById(notificationId).orElseThrow(() ->
          new IllegalArgumentException("Notification 없음 " + notificationId));
      ;
      return notificationMapper.toDto(notification);
    } catch (NotificationCreateError e) {
      log.warn("알림 오류", e);
      throw new NotificationCreateError(
          ErrorCode.FAIL_CREATE_NOTIFICATION);
    }
  }

  @Override
  @Transactional(readOnly = true) //페이지네이션
  public PageResponse getPageNation(UUID receiverId, String cursor, int limit) {

    try {
      List<Notification> slice;
      if (cursor == null || cursor.isBlank()) {
        slice = repository.findByReceiverIdOrderByCreatedAtDesc(
            receiverId,
            PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
      } else {
        Instant before = Instant.parse(cursor);
        slice = repository.findByReceiverIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            receiverId,
            before,
            PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
      }

      List<NotificationDto> dtos = slice.stream()
          .map(notificationMapper::toDto)
          .toList();

      boolean hasNext = dtos.size() == limit;
      String nextCursor = null;
      UUID nextIdAfter = null;

      if (hasNext) {
        NotificationDto last = dtos.get(dtos.size() - 1);
        nextCursor = last.createdAt().toString();
        nextIdAfter = last.id();
      }
      int totalCount = repository.countByReceiverId(receiverId); //나중에 캐시 넣기
      return new PageResponse(
          dtos,
          hasNext,
          nextCursor,
          nextIdAfter,
          "createdAt",
          "DESC",
          totalCount
      );
    } catch (NotificationPaginationError e) {
      log.warn("알림 오류", e);
      throw new NotificationPaginationError(
          ErrorCode.FAIL_GET_PAGINATION_NOTIFICATION, e);
    }
  }

  @Override
  @Transactional
  @CacheEvict(key = "#notificationId")
  public void readNotification(UUID notificationId) {
    try {
      Notification notification = repository.findById(notificationId).orElseThrow(() ->
          new NotFoundNotification(ErrorCode.NOT_FOUND_NOTIFICATION));
      repository.delete(notification);
    } catch (FailReadNotification e) {
      log.warn("알림 오류", e);
      throw new FailReadNotification(
          ErrorCode.FAIL_READ_NOTIFICATION, e);
    }
  }

  @Transactional(propagation = Propagation.NOT_SUPPORTED)//벌크용 몽고db에 한번에 쓰기
  public List<NotificationDto> createAll(List<NotificationRequest> reqs) {

    List<NotificationRequest> waitList = new ArrayList<>(reqs);//넣는거 대기
    List<NotificationDto> success = new ArrayList<>();//넣는데 성공한거, 반환용

    for (int attempt = 0; attempt < 2 && !waitList.isEmpty(); attempt++) {//2번시도

      BulkOperations ops = mongoTemplate
          .bulkOps(BulkOperations.BulkMode.UNORDERED, Notification.class);

      waitList.forEach(r -> ops.insert(toDocument(r)));

      try {
        BulkWriteResult wr = ops.execute();
        success.addAll(
            waitList.stream()
                .map(this::toDocument)
                .map(notificationMapper::toDto)
                .toList()
        );
        waitList.clear();//다 넣고, 비우기

      } catch (BulkOperationException ex) { //실패한거 있으면 해당 인덱스 반환
        log.warn("알림 벌크 작업 1차 실패");
        Set<Integer> failedIdx = new HashSet<>();

        for (BulkWriteError error : ex.getErrors()) {   // BulkWriteError 목록 순회
          failedIdx.add(error.getIndex());        // 실패한거 index 추출
        }

        List<NotificationRequest> nextRound = new ArrayList<>();//실패란거 처리
        for (int i = 0; i < waitList.size(); i++) {
          NotificationRequest req = waitList.get(i);
          if (req.level() == NotificationLevel.INFO) {//info레벨 이상
            log.error("알림생성 실패", req);
          } else {
            if (failedIdx.contains(i)) {
              nextRound.add(req);                    // 재시도 대상
            } else {                                 // 성공 건
              success.add(notificationMapper.toDto(toDocument(req)));
            }
          }
        }
        waitList = nextRound;                         // 재시도 목록 또 넣기
      }
    }
    if (!waitList.isEmpty()) {
      log.error("Bulk 작업 2회 시도 후에도 {}건 실패 ", waitList.size());
      throw new NotificationCreateError(ErrorCode.FAIL_CREATE_BULK_NOTIFICATION);
    }
    return success;
  }

  private Notification toDocument(NotificationRequest req) {
    return Notification.createNotification(req);
  }

}
