package com.example.ootd;

import com.example.ootd.domain.user.util.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class OotdApplicationTests {

  @MockitoBean
  private EmailService emailService;
  @MockitoBean
  private JavaMailSender sender;
  @Test
  void contextLoads() {
  }

}
