package com.example.ootd.domain.user;


import static org.assertj.core.api.Assertions.*;

import com.example.ootd.domain.image.service.S3Service;
import com.example.ootd.domain.user.dto.UserSearchCondition;
import com.example.ootd.domain.user.repository.UserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
public class UserRepositoryTest {


  @Autowired
  EntityManager em;

  @Autowired
  UserRepository userRepository;
  @MockitoBean
  S3Service s3service;

  User u1;
  User u2;
  User u3;

  @TestConfiguration
  static class QuerydslTestConfig {
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager em) {
      return new JPAQueryFactory(em);
    }
  }

  @BeforeEach
  void setUp(){
    u1 = new User("u1", "u1@email.com", "1234");
    u1.updateRole(UserRole.ROLE_ADMIN);
    //ReflectionTestUtils.setField(u1, "createdAt", LocalDateTime.now().minusDays(3) );

    u2 = new User("u2", "u2@email.com", "1234");
    //ReflectionTestUtils.setField(u2, "createdAt", LocalDateTime.now().minusDays(5) );

    u3 = new User("u3", "u3@email.com", "1234");
    //ReflectionTestUtils.setField(u3, "createdAt", LocalDateTime.now().minusDays(2) );

    em.persist(u1);
    em.persist(u2);
    em.persist(u3);
    em.flush();

    ReflectionTestUtils.setField(u1, "createdAt", LocalDateTime.now().minusDays(3) );
    ReflectionTestUtils.setField(u2, "createdAt", LocalDateTime.now().minusDays(5) );
    ReflectionTestUtils.setField(u3, "createdAt", LocalDateTime.now().minusDays(2) );

    em.flush();
    em.clear();

  }

  @Test
  @DisplayName("조건 없이 전체 조회 최신순 정렬")
  void 조건_없이_전체_조회_최신순_정렬(){
    UserSearchCondition condition = new UserSearchCondition(
        null, null, 3, "createdAt", "ASCENDING", null, null, null
    );

    List<User> result = userRepository.searchUserOfCondition(condition);


    assertThat(result).hasSize(3);
    assertThat(result.get(0).getName()).isEqualTo("u2");
    assertThat(result.get(1).getName()).isEqualTo("u1");
  }

  @Test
  @DisplayName("이메일 like 조건 조회")
  void 이메일_like_조건_조회(){
    UserSearchCondition condition = new UserSearchCondition(
        null, null, 3, "createdAt", "ASCENDING", "u1", null, null
    );

    List<User> result = userRepository.searchUserOfCondition(condition);

    assertThat(result).hasSize(1);
    result
        .forEach(u -> assertThat(u.getEmail()).containsIgnoringCase("u1"));
  }

  @Test
  @DisplayName("권한 조건 조회")
  void 권한_조건_조회(){
    UserSearchCondition condition = new UserSearchCondition(
        null, null, 3, "createdAt", "ASCENDING", null, UserRole.ROLE_ADMIN, null
    );

    UserSearchCondition condition2 = new UserSearchCondition(
        null, null, 3, "createdAt", "ASCENDING", null, UserRole.ROLE_USER, null
    );

    List<User> result = userRepository.searchUserOfCondition(condition);
    List<User> result2 = userRepository.searchUserOfCondition(condition2);
    assertThat(result).hasSize(1);
    assertThat(result2).hasSize(2);
  }
}
