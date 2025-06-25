package com.example.ootd.domain.notification.mapper;

import com.example.ootd.domain.notification.dto.NotificationDto;
import com.example.ootd.domain.notification.entity.Notification;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

  @Mapping(target = "id", source = "id")
  @Mapping(target = "receiverId", source = "receiverId")
  @Mapping(target = "title", source = "title")
  @Mapping(target = "content", source = "content")
  @Mapping(target = "level", source = "level")
  @Mapping(target = "createdAt", source = "createdAt")
  NotificationDto toDto(Notification entity);

  List<NotificationDto> toDtoList(List<Notification> entities);
}