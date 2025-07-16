package com.example.ootd.domain.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.example.ootd.domain.image.entity.Image;
import com.example.ootd.domain.message.dto.DirectMessageDto;
import com.example.ootd.domain.message.dto.UserMessageInfo;
import com.example.ootd.domain.message.entity.Message;
import com.example.ootd.domain.message.mapper.MessageMapper;
import com.example.ootd.domain.user.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageMapperTest {

  private final MessageMapper mapper = Mappers.getMapper(MessageMapper.class);

  private static final UUID SENDER_ID = UUID.randomUUID();
  private static final UUID RECEIVER_ID = UUID.randomUUID();
  private static final LocalDateTime FIXED = LocalDateTime.of(2025, 7, 16, 14, 0);

  @Nested
  class UserInfoMapping {

    @Test
    void userToInfo_allFields() {

      User sender = mockUser(SENDER_ID, "aaaa", "https://example.com/alice.jpg");

      UserMessageInfo info = mapper.toMessageInfo(sender);
      assertThat(info.userId()).isEqualTo(SENDER_ID);
      assertThat(info.name()).isEqualTo("aaaa");
      assertThat(info.profileImageUrl()).isEqualTo("https://example.com/alice.jpg");
    }

    @Test
    void userToInfo_noImage() {
      User sender = mockUser(SENDER_ID, "aaaa", null);

      UserMessageInfo info = mapper.toMessageInfo(sender);

      assertThat(info.profileImageUrl()).isNull();
    }
  }

  // ----------------------------------------------------------- Message → DTO
  @Nested
  class MessageToDtoMapping {

    @Test
    void messageToDto_basic() {
      User sender = mockUser(SENDER_ID, "aaaa", "https://e.com/a.jpg");
      User receiver = mockUser(RECEIVER_ID, "bbbb", "https://e.com/b.jpg");

      Message message = mock(Message.class, Answers.RETURNS_DEEP_STUBS);
      when(message.getId()).thenReturn(UUID.randomUUID());
      when(message.getCreatedAt()).thenReturn(FIXED);
      when(message.getContent()).thenReturn("hhhhh");
      when(message.getSender()).thenReturn(sender);
      when(message.getReceiver()).thenReturn(receiver);

      DirectMessageDto dto = mapper.toDto(message);

      assertThat(dto.content()).isEqualTo("hhhhh");
      assertThat(dto.createdAt()).isEqualTo(FIXED);
      assertThat(dto.sender().name()).isEqualTo("aaaa");
      assertThat(dto.receiver().userId()).isEqualTo(RECEIVER_ID);
    }
  }


  private static User mockUser(UUID id, String name, String imageUrl) {
    User user = mock(User.class, Answers.RETURNS_DEEP_STUBS);
    when(user.getId()).thenReturn(id);
    when(user.getName()).thenReturn(name);

    if (imageUrl != null) {
      Image img = mock(Image.class, Answers.RETURNS_DEEP_STUBS);
      when(img.getUrl()).thenReturn(imageUrl);
      when(user.getImage()).thenReturn(img);
    } else {
      when(user.getImage()).thenReturn(null);
    }
    return user;
  }
}