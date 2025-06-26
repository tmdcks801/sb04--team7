package com.example.ootd.domain.notification.service.impli;

//@Slf4j
//@Component
//@RequiredArgsConstructor
public class NotificationEventConsumer {

//카프카를 위해 주석

//  private final EmitterRepository emitters;
//  private final NotificationServiceInterface notificationService;
//  private final NotificationMapper notificationMapper;
//
//
//  @KafkaListener(topics = "notification-events", containerFactory = "kafkaListenerContainerFactory")
//  @Transactional //일단은 동기임
//  public void onMessage(NotificationDto dto, Acknowledgment ack) {
//    try {
//      notificationService.createNotification(dto);
//      emitters.send(dto.receiverId(), dto);
//      ack.acknowledge();
//    } catch (Exception ex) {
//      log.error("Failed to push SSE for notification {}", dto.id(), ex);
//      throw ex;
//    }
//  }


}
