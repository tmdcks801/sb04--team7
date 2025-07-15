package com.example.ootd.domain.message.service;

import com.example.ootd.domain.message.dto.DirectMessageDto;
import com.example.ootd.domain.message.dto.MessagePaginationDto;
import com.example.ootd.domain.message.entity.Message;
import com.example.ootd.domain.message.mapper.MessageMapper;
import com.example.ootd.domain.message.repository.MessageRepository;
import com.example.ootd.domain.notification.dto.NotificationRequest;
import com.example.ootd.domain.notification.enums.NotificationLevel;
import com.example.ootd.domain.notification.service.inter.NotificationPublisherInterface;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.dto.PageResponse;
import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.message.FailGetMessageExecption;
import com.example.ootd.exception.message.FailSendMessageException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class MessageServiceImp implements MessageServiceInterface {

  private final MessageRepository messageRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final MessageMapper messageMapper;
  private final UserRepository userRepository;
  private final NotificationPublisherInterface notificationPublisherInterface;


  @Override
  @Transactional
  public DirectMessageDto sendMessage(UUID senderId, UUID receiverId, String content) {

    try {
      User sender = userRepository.findById(senderId)
          .orElseThrow(() -> new IllegalArgumentException("Sender 없음 : " + senderId));
      User receiver = userRepository.findById(receiverId)
          .orElseThrow(() -> new IllegalArgumentException("Receiver 없음 : " + receiverId));
      Message message = Message.createMessage(sender, receiver, content);
      messageRepository.save(message);
      DirectMessageDto dto = messageMapper.toDto(message);
      messagingTemplate.convertAndSend("/sub/direct-messages_" + message.getDmKey(), dto);
      notificationPublisherInterface.publish(
          new NotificationRequest(receiverId, sender.getName() + "님으부터 메세지", content,
              NotificationLevel.INFO));//이벤트
      return dto;
    } catch (FailSendMessageException e) {
      log.error("메세지 전송중 오류");
      throw new FailSendMessageException(ErrorCode.FAIL_SEND_MESSAGE);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse getMessage(MessagePaginationDto req) {
    try {
      String dmKey = Message.makeDmKey(req.sender(), req.receiver());
      int limit = Math.max(req.limit(), 1);

      Sort sort = Sort.by(Sort.Direction.DESC, "createdAt")
          .and(Sort.by(Sort.Direction.DESC, "id"));
      Pageable page = PageRequest.of(0, limit + 1, sort);

      List<Message> msgs;

      if (req.cursor() == null) {
        msgs = messageRepository.findByDmKey(dmKey, page);
      } else if (req.isAfter()) {
        Message pivot = messageRepository.findById(req.cursor())
            .orElseThrow(() -> new IllegalArgumentException("잘못된 cursor"));
        msgs = messageRepository.findNewerThan(
            dmKey, pivot.getCreatedAt(), pivot.getId(), page);

        Collections.reverse(msgs);
      } else {
        Message pivot = messageRepository.findById(req.cursor())
            .orElseThrow(() -> new IllegalArgumentException("잘못된 cursor"));
        msgs = messageRepository.findOlderThan(
            dmKey, pivot.getCreatedAt(), pivot.getId(), page);
      }

      boolean hasNext = msgs.size() > limit;
      if (hasNext) {
        msgs = msgs.subList(0, limit);
      }

      List<DirectMessageDto> data = msgs.stream()
          .map(messageMapper::toDto)
          .toList();

      UUID nextCursor = hasNext && !msgs.isEmpty()
          ? msgs.get(msgs.size() - 1).getId()
          : null;

      long total = messageRepository.countByDmKey(dmKey);

      return new PageResponse(
          data, hasNext, nextCursor, null,
          "createdAt", req.isAfter() ? "ASC" : "DESC", total);
    } catch (FailGetMessageExecption e) {
      throw new FailGetMessageExecption(ErrorCode.FAIL_GET_MESSAGE);
    }
  }
}
