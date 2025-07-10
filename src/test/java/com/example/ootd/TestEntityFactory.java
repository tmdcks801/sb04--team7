package com.example.ootd;

import com.example.ootd.domain.user.Gender;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.UserRole;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

public class TestEntityFactory {

  public static User createUser(){
    User user = new User(
        "test-name",
        "test@gmail.com",
        "test-password",
        UserRole.ROLE_USER,
        false,
        null,
        null,
        Gender.MALE,
        LocalDate.of(1999,4,13),
        3,
        false,
        null
    );

    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

    return user;
  }

  public static User createUser(String email, String name){
    User user = new User(
        name,
        email,
        "test-password",
        UserRole.ROLE_USER,
        false,
        null,
        null,
        Gender.MALE,
        LocalDate.of(1999,4,13),
        3,
        false,
        null
    );

    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.now());

    return user;
  }
}
