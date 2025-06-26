package com.example.ootd.domain.follow.controller;

import com.example.ootd.domain.follow.service.FollowService;
import com.example.ootd.domain.user.User;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

//TODO : 빌드 성공 시 테스트 코드 작성 예정
@ExtendWith(MockitoExtension.class)
public class FollowControllerTest {

  private MockMvc mockMvc;

  @Mock private FollowService followService;

  @Mock private User user;

  private FollowController followController;
}
