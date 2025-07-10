package com.example.ootd.domain.message;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.ootd.domain.message.dto.DirectMessageDto;
import com.example.ootd.domain.message.dto.MessagePaginationDto;
import com.example.ootd.domain.message.entity.Message;
import com.example.ootd.domain.message.mapper.MessageMapper;
import com.example.ootd.domain.message.repository.MessageRepository;
import com.example.ootd.domain.message.service.MessageServiceImp;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.dto.PageResponse;
import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.message.FailGetMessageExecption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class MessageServiceImpTest {

  @Mock
  MessageRepository messageRepository;
  @Mock
  SimpMessagingTemplate messagingTemplate;
  @Mock
  MessageMapper messageMapper;
  @Mock
  UserRepository userRepository;

  @InjectMocks
  MessageServiceImp messageService;

  @Test
  void sendMessage_success() {
    UUID senderId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();

    User sender = mock(User.class);
    User receiver = mock(User.class);
    when(sender.getId()).thenReturn(senderId);
    when(receiver.getId()).thenReturn(receiverId);

    when(userRepository.findById(eq(senderId))).thenReturn(Optional.of(sender));
    when(userRepository.findById(eq(receiverId))).thenReturn(Optional.of(receiver));

    when(messageRepository.save(any(Message.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    DirectMessageDto expectedDto = mock(DirectMessageDto.class);
    when(messageMapper.toDto(any(Message.class))).thenReturn(expectedDto);

    DirectMessageDto result =
        messageService.sendMessage(senderId, receiverId, "hello");

    assertSame(expectedDto, result);
    verify(messageRepository).save(any(Message.class));
    verify(messagingTemplate)
        .convertAndSend(
            argThat(path -> path.startsWith("/sub/direct-messages_")),
            eq(expectedDto)
        );
  }

  @Test
  void sendMessage_senderNotFound_throwsIllegalArgumentException() {
    UUID senderId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();

    when(userRepository.findById(senderId)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> messageService.sendMessage(senderId, receiverId, "hi")
    );
  }

  @Test
  void getMessage_firstPage_success() {
    UUID s = UUID.randomUUID();
    UUID r = UUID.randomUUID();
    MessagePaginationDto req = new MessagePaginationDto(s, r, null, false, 20);

    Message msg = mock(Message.class);

    when(messageRepository.findByDmKey(anyString(), any(Pageable.class)))
        .thenReturn(List.of(msg));
    DirectMessageDto dto = mock(DirectMessageDto.class);
    when(messageMapper.toDto(msg)).thenReturn(dto);

    PageResponse res = messageService.getMessage(req);

    assertFalse(res.hasNext());
    assertEquals(1, res.data().size());
    assertSame(dto, res.data().get(0));
  }


  @Test
  void getMessage_repositoryThrows_failGetMessageException() {
    UUID s = UUID.randomUUID();
    UUID r = UUID.randomUUID();
    MessagePaginationDto req = new MessagePaginationDto(s, r, null, false, 20);

    when(messageRepository.findByDmKey(anyString(), any(Pageable.class)))
        .thenThrow(new FailGetMessageExecption(ErrorCode.FAIL_GET_MESSAGE));

    assertThrows(
        FailGetMessageExecption.class,
        () -> messageService.getMessage(req)
    );
  }
}

