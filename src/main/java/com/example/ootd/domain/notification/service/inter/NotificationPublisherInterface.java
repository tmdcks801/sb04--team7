package com.example.ootd.domain.notification.service.inter;


import com.example.ootd.domain.notification.dto.NotificationEvent;
import com.example.ootd.domain.notification.dto.NotificationRequest;
import java.util.List;
import java.util.UUID;

public interface NotificationPublisherInterface {

 //이벤트 발행, 다른 서비스에서 이벤트 발생시킬떄 이거쓰면 됨
  void publish(NotificationRequest event);

  //리스트안 사람한테 알람
  void publishToMany(NotificationEvent event, List<UUID> userIdList);

  //유저 모두한테 알람
  void publishToAll(NotificationEvent event);
}
