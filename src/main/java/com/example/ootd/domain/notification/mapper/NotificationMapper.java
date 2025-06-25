package com.example.ootd.domain.notification.mapper;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.entity.Notification;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

  NotificationDto toDto(Notification entity);

  List<NotificationDto> toDtoList(List<Notification> entities);
}