package com.example.ootd.domain.notification.service.inter;


import com.example.ootd.domain.notification.dto.NotificationRequest;

public interface NotificationPublisherInterface {

  //이벤트 발행, 다른 서비스에서 이벤트 발생시킬떄 이거쓰면 됨
  void publish(NotificationRequest event);
}
