package com.example.ootd.domain.message;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ootd.domain.image.service.S3Service;
import com.example.ootd.domain.message.entity.Message;
import com.example.ootd.domain.message.repository.MessageRepository;
import com.example.ootd.domain.user.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@ActiveProfiles("test")
class MessageRepositoryTest {

  @MockitoBean
  S3Service s3Service;

  @MockitoBean
  JPAQueryFactory jpaQueryFactory;

  @Autowired
  MessageRepository messageRepository;

  @Autowired
  org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager em;


  User aaaa;
  User bbbb;
  String dmKey;

  @BeforeEach
  void setUp() throws Exception {
    aaaa = createUser("aaaa");
    bbbb = createUser("bbbb");
    dmKey = Message.makeDmKey(aaaa.getId(), bbbb.getId());

    for (int i = 0; i < 5; i++) {
      Message m = Message.builder()
          .sender(aaaa)
          .receiver(bbbb)
          .content("test-" + i)
          .build();
      setField(m, "createdAt", LocalDateTime.now().minusSeconds(5 - i));
      em.persist(m);
    }
    em.flush();
    em.clear();
  }

  @Nested
  class findByDmKey {

    @Test
    void returnsMessagesNewestFirst() {
      List<Message> page =
          messageRepository.findByDmKey(
              dmKey,
              PageRequest.of(
                  0, 3,
                  Sort.by(Sort.Direction.DESC, "createdAt", "id")
              )
          );

      assertThat(page).hasSize(3);
      assertThat(page)
          .extracting(Message::getCreatedAt)
          .isSortedAccordingTo(Comparator.reverseOrder());
    }
  }

  @Nested
  class PivotQueries {

    @Test
    void sliceAroundPivot() {
      List<Message> all = messageRepository.findByDmKey(dmKey, PageRequest.of(0, 10));
      Message pivot = all.get(2);

      List<Message> older =
          messageRepository.findOlderThan(dmKey, pivot.getCreatedAt(), pivot.getId(),
              PageRequest.of(0, 10));
      List<Message> newer =
          messageRepository.findNewerThan(dmKey, pivot.getCreatedAt(), pivot.getId(),
              PageRequest.of(0, 10));

      assertThat(older).extracting(Message::getContent)
          .containsExactly("test-0", "test-1");
      assertThat(newer).extracting(Message::getContent)
          .containsExactly("test-3", "test-4");
    }
  }

  @Test
  void count() {
    long cnt = messageRepository.countByDmKey(dmKey);
    assertThat(cnt).isEqualTo(5);
  }


  private User createUser(String nickname) throws Exception {
    Constructor<User> ctor = User.class.getDeclaredConstructor();
    ctor.setAccessible(true);
    User u = ctor.newInstance();

    setField(u, "email", UUID.randomUUID() + "@example.com");
    setField(u, "name", nickname);

    try {
      setField(u, "nickname", nickname);
    } catch (NoSuchFieldException ignore) {
    }

    em.persist(u);
    em.flush();

    return u;
  }

  private static void setField(Object target, String name, Object value)
      throws Exception {
    Field f = target.getClass().getDeclaredField(name);
    f.setAccessible(true);
    f.set(target, value);
  }
}