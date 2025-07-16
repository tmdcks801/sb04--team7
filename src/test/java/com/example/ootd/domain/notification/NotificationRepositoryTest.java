//package com.example.ootd.domain.notification;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import com.example.ootd.domain.notification.entity.Notification;
//import com.example.ootd.domain.notification.enums.NotificationLevel;
//import com.example.ootd.domain.notification.repository.NotificationRepository;
//import java.time.Instant;
//import java.time.temporal.ChronoUnit;
//import java.util.List;
//import java.util.UUID;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//
//
//@DataMongoTest
//class NotificationRepositoryTest {
//
//  @Autowired
//  private NotificationRepository notificationRepository;
//
//  private UUID receiver1;
//  private UUID receiver2;
//  private Instant now;
//
//  @BeforeEach
//  void setUp() {
//    notificationRepository.deleteAll();
//
//    receiver1 = UUID.randomUUID();
//    receiver2 = UUID.randomUUID();
//    now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
//
//    notificationRepository.saveAll(List.of(
//        buildNotification(receiver1, now),
//        buildNotification(receiver1, now.minus(1, ChronoUnit.HOURS)),
//        buildNotification(receiver1, now.minus(2, ChronoUnit.HOURS))
//    ));
//
//    notificationRepository.save(buildNotification(receiver2, now));
//  }
//
//  private Notification buildNotification(UUID receiverId, Instant createdAt) {
//    return Notification.builder()
//        .id(UUID.randomUUID())
//        .receiverId(receiverId)
//        .title("알림 제목")
//        .content("알림 본문")
//        .level(NotificationLevel.INFO)
//        .createdAt(createdAt)
//        .build();
//  }
//
//  @Test
//  void countByReceiverId() {
//    assertThat(notificationRepository.countByReceiverId(receiver1)).isEqualTo(3);
//    assertThat(notificationRepository.countByReceiverId(receiver2)).isEqualTo(1);
//  }
//
//  @Test
//  void findByReceiverIdOrderByCreatedAtDesc() {
//    List<Notification> notifications = notificationRepository
//        .findByReceiverIdOrderByCreatedAtDesc(receiver1, PageRequest.of(0, 10));
//
//    assertThat(notifications).hasSize(3);
//    assertThat(notifications.get(0).getCreatedAt()).isEqualTo(now);
//    assertThat(notifications.get(2).getCreatedAt()).isEqualTo(now.minus(2, ChronoUnit.HOURS));
//  }
//
//  @Test
//  void findByReceiverIdAndBefore() {
//    Pageable pageable = PageRequest.of(0, 10);
//    Instant before = now.minus(30, ChronoUnit.MINUTES);
//    List<Notification> notifications = notificationRepository
//        .findByReceiverIdAndCreatedAtBeforeOrderByCreatedAtDesc(receiver1, before, pageable);
//
//    assertThat(notifications).hasSize(2);
//    assertThat(notifications.get(0).getCreatedAt()).isEqualTo(now.minus(1, ChronoUnit.HOURS));
//  }
//
//  @Test
//  void findAllByReceiverIdAndAfter() {
//    Instant after = now.minus(90, ChronoUnit.MINUTES);
//    List<Notification> notifications = notificationRepository
//        .findAllByReceiverIdAndCreatedAtGreaterThanOrderByCreatedAtAsc(receiver1, after);
//
//    assertThat(notifications).hasSize(2);
//    assertThat(notifications.get(0).getCreatedAt()).isEqualTo(now.minus(1, ChronoUnit.HOURS));
//    assertThat(notifications.get(1).getCreatedAt()).isEqualTo(now);
//  }
//}
//
