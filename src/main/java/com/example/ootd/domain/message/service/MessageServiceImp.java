package com.example.ootd.domain.message.service;

import com.example.ootd.domain.message.dto.DirectMessageDto;
import com.example.ootd.domain.message.dto.MessagePaginationDto;
import com.example.ootd.domain.message.entity.Message;
import com.example.ootd.domain.message.mapper.MessageMapper;
import com.example.ootd.domain.message.repository.MessageRepository;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.domain.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class MessageServiceImp implements MessageServiceInterface {

  private final MessageRepository messageRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final MessageMapper messageMapper;
  private final UserRepository userRepository;

  @Override
  public DirectMessageDto sendMessage(UUID senderId, UUID receiverId, String content) {

    User sender = userRepository.findById(senderId)
        .orElseThrow(() -> new IllegalArgumentException("Sender 없음 : " + senderId));
    User receiver = userRepository.findById(receiverId)
        .orElseThrow(() -> new IllegalArgumentException("Receiver 없음 : " + receiverId));
    Message message = Message.createMessage(sender, receiver, content);
    messageRepository.save(message);
    DirectMessageDto dto = messageMapper.toDto(message);

    try {
      messagingTemplate.convertAndSend("/sub/direct-messages_" + message.getDmKey(), dto);
    } catch (Exception brokerEx) {
      log.warn("메세지 오류 나중에 내용 이입",
          brokerEx);
    }
    return dto;
  }

  @Override
  public List<DirectMessageDto> getMessage(MessagePaginationDto req) {
    try {
      String dmKey = Message.makeDmKey(req.sender(), req.receiver());

      Sort sort = req.isAfter() ? Sort.by("id").ascending() : Sort.by("id").descending();
      Pageable pageable = PageRequest.of(0, req.limit(), sort);

      List<Message> messages;
      if (req.cursor() == null) {
        messages = messageRepository.findByDmKey(dmKey, pageable);
      } else if (req.isAfter()) {
        messages = messageRepository.findByDmKeyAndIdGreaterThan(dmKey, req.cursor(), pageable);
      } else {
        messages = messageRepository.findByDmKeyAndIdLessThan(dmKey, req.cursor(), pageable);
      }

      if (messages.isEmpty()) {
        return Collections.emptyList();
      }

      // 클라이언트 편의를 위해 원하는 방향으로 정렬
      if (!req.isAfter()) {
        Collections.reverse(messages);
      }

      return messages.stream()
          .map(messageMapper::toDto)
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("메세지 오류 나중에 상세 기입");
      throw new IllegalArgumentException(
          "메시지 조회 중 오류 발생", e);
    }
  }

}
