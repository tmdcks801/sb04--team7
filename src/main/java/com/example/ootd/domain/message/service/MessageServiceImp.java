package com.example.ootd.domain.message.service;

import com.example.ootd.domain.message.dto.DirectMessageDto;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MessageServiceImp implements MessageServiceInterface {

  private final MessageRepository messageRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final MessageMapper messageMapper;
  private final UserRepository userRepository;

  @Override
  public DirectMessageDto sendMessage(UUID senderId, UUID receiverId, String content) {

    User sender = userRepository.findById(senderId)
        .orElseThrow(() -> new EntityNotFoundException("Sender 없음"));
    User receiver = userRepository.findById(receiverId)
        .orElseThrow(() -> new EntityNotFoundException("Receiver 없음"));

    Message message = Message.createMessage(sender, receiver, content);
    messageRepository.save(message);
    DirectMessageDto directMessageDto = messageMapper.toDto(message);

    messagingTemplate.convertAndSend("/sub/direct-messages_"
        + message.getDmKey(), directMessageDto);
    return directMessageDto;
  }

  @Override
  public List<DirectMessageDto> getMessage(UUID sender, UUID receiver, UUID cursor, boolean isAfter,
      int limit) {
    String dmKey = makeDmKey(sender, receiver);

    Sort sort = isAfter ? Sort.by("id").ascending() : Sort.by("id").descending();
    Pageable pageable = PageRequest.of(0, limit, sort);

    List<Message> messages;
    if (cursor == null) {
      messages = messageRepository.findByDmKey(dmKey, pageable);
    } else if (isAfter) {
      messages = messageRepository.findByDmKeyAndIdGreaterThan(dmKey, cursor, pageable);
    } else {
      messages = messageRepository.findByDmKeyAndIdLessThan(dmKey, cursor, pageable);
    }

    // ensure ascending order before mapping
    if (!isAfter) {
      Collections.reverse(messages);
    }

    return messages.stream()
        .map(messageMapper::toDto)
        .collect(Collectors.toList());
  }

  private String makeDmKey(UUID first, UUID second) {
    String a = first.toString();
    String b = second.toString();

    if (a.compareTo(b) < 0) {
      return a + '_' + b;
    } else {
      return b + '_' + a;
    }
  }
}
