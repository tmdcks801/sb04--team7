package com.example.ootd.domain.message.mapper;

import com.example.ootd.domain.message.dto.DirectMessageDto;
import com.example.ootd.domain.message.dto.UserMessageInfo;
import com.example.ootd.domain.message.entity.Message;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface MessageMapper {

  //이따 추가
  @Mapping(target = "id", source = "id")
  @Mapping(target = "createdAt", source = "createdAt")
  @Mapping(target = "content", source = "content")
  //유저랑 이미지 완성되어야 확인 될꺼같음
  @Mapping(target = "sender", expression = "java(toMessageInfo(message.getSender()))")
//유저랑 이미지 완성되어야 확인 될꺼같음
  @Mapping(target = "receiver", expression = "java(toMessageInfo(message.getReceiver()))")
  DirectMessageDto toDto(Message message);

  //진짜 코드
  @Mapping(target = "userId", source = "id")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "profileImageUrl", source = "image.url")
  //더미로 테스트
//  @Mapping(target = "userId", constant = "a3127131-702e-63fa-b22e-fb3fdea19fe3")
//  @Mapping(target = "name", source = "name")
//  @Mapping(target = "profileImageUrl", constant = "https://example.com/profile.png")
  UserMessageInfo toMessageInfo(User user);


}
