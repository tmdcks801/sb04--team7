package com.example.ootd.domain.feed.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ootd.TestEntityFactory;
import com.example.ootd.TestPrincipalUser;
import com.example.ootd.domain.feed.dto.data.CommentDto;
import com.example.ootd.domain.feed.dto.data.FeedDto;
import com.example.ootd.domain.feed.dto.request.CommentCreateRequest;
import com.example.ootd.domain.feed.dto.request.FeedCreateRequest;
import com.example.ootd.domain.feed.dto.request.FeedSearchCondition;
import com.example.ootd.domain.feed.dto.request.FeedUpdateRequest;
import com.example.ootd.domain.feed.entity.Feed;
import com.example.ootd.domain.feed.service.FeedService;
import com.example.ootd.domain.sse.service.SsePushServiceInterface;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.dto.AuthorDto;
import com.example.ootd.domain.weather.dto.PrecipitationDto;
import com.example.ootd.domain.weather.dto.TemperatureDto;
import com.example.ootd.domain.weather.dto.WeatherSummaryDto;
import com.example.ootd.domain.weather.entity.PrecipitationType;
import com.example.ootd.domain.weather.entity.SkyStatus;
import com.example.ootd.domain.weather.entity.Weather;
import com.example.ootd.dto.PageResponse;
import com.example.ootd.security.PrincipalUser;
import com.example.ootd.security.jwt.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(FeedController.class)
@WithMockUser(roles = "USER")
class

FeedControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockitoBean
  FeedService feedService;
  @MockitoBean
  private SsePushServiceInterface ssePushService;
  @MockitoBean
  private JwtService jwtService;
  @MockitoBean
  private AuthenticationManager authenticationManager;
  @MockitoBean
  private UserDetailsService userDetailsService;

  private User user;
  private Weather weather;
  private Feed feed;
  private UUID feedId;

  @BeforeEach
  void setUp() {
    user = TestEntityFactory.createUser();
    weather = TestEntityFactory.createWeather(SkyStatus.CLOUDY, PrecipitationType.NONE);
    feedId = UUID.randomUUID();

    User mockUser = Mockito.mock(User.class);
    given(mockUser.getId()).willReturn(user.getId());
    PrincipalUser principalUser = new TestPrincipalUser(mockUser);

    Authentication authentication = new UsernamePasswordAuthenticationToken(
        principalUser,
        null,
        principalUser.getAuthorities()
    );
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @Nested
  class FeedApi {

    @Test
    @DisplayName("피드 등록 성공")
    void createFeed() throws Exception {
      FeedCreateRequest request = new FeedCreateRequest(user.getId(), weather.getId(),
          Collections.emptyList(), "new content");

      FeedDto response = FeedDto.builder()
          .id(feedId)
          .createdAt(LocalDateTime.now())
          .updatedAt(LocalDateTime.now())
          .author(new AuthorDto(user.getId(), user.getName(), null))
          .weather(new WeatherSummaryDto(
              weather.getId(),
              weather.getSkyStatus(),
              new PrecipitationDto(weather.getPrecipitation().getPrecipitationType(), 0, 0),
              new TemperatureDto(0, 0, 0, 0)
          ))
          .ootds(Collections.emptyList())
          .content("new content")
          .likeCount(0)
          .commentCount(0)
          .likedByMe(false)
          .build();

      given(feedService.createFeed(request)).willReturn(response);

      mockMvc.perform(MockMvcRequestBuilders.post("/api/feeds")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.content").value("new content"))
          .andExpect(jsonPath("$.author.userId").value(user.getId().toString()));
    }

    @Test
    @DisplayName("피드 수정 성공")
    void updateFeed() throws Exception {
      FeedUpdateRequest request = new FeedUpdateRequest("updated content");

      FeedDto updated = FeedDto.builder()
          .id(feedId)
          .createdAt(LocalDateTime.now())
          .updatedAt(LocalDateTime.now())
          .author(new AuthorDto(user.getId(), user.getName(), null))
          .weather(null)
          .ootds(Collections.emptyList())
          .content("updated content")
          .likeCount(0)
          .commentCount(0)
          .likedByMe(false)
          .build();

      given(feedService.updateFeed(feedId, request, user.getId())).willReturn(updated);

      mockMvc.perform(MockMvcRequestBuilders.patch("/api/feeds/{feedId}", feedId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").value("updated content"));
    }

    @Test
    @DisplayName("피드 목록 조회")
    void findFeed() throws Exception {
      FeedDto feedDto = FeedDto.builder()
          .id(feedId)
          .createdAt(LocalDateTime.now())
          .updatedAt(LocalDateTime.now())
          .author(new AuthorDto(user.getId(), user.getName(), null))
          .weather(null)
          .ootds(Collections.emptyList())
          .content("피드 내용")
          .likeCount(0)
          .commentCount(0)
          .likedByMe(false)
          .build();

      PageResponse<FeedDto> response = new PageResponse<>(
          Collections.singletonList(feedDto),
          false,
          null,
          null,
          "createdAt",
          "DESCENDING",
          1L
      );

      FeedSearchCondition condition = FeedSearchCondition.builder().sortBy("createdAt")
          .sortDirection("DESCENDING").limit(10).build();

      given(feedService.findFeedByCondition(condition, user.getId())).willReturn(response);

      mockMvc.perform(MockMvcRequestBuilders.get("/api/feeds")
              .param("limit", "10")
              .param("sortBy", "createdAt")
              .param("sortDirection", "DESCENDING")
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data[0].content").value("피드 내용"));
    }
  }

  @Nested
  class FeedCommentApi {

    @Test
    @DisplayName("댓글 등록 성공")
    void createComment() throws Exception {
      CommentCreateRequest request = new CommentCreateRequest(feedId, user.getId(), "댓글입니다");

      CommentDto response = CommentDto.builder()
          .id(UUID.randomUUID())
          .content("댓글입니다")
          .author(new AuthorDto(user.getId(), user.getName(), null))
          .feedId(feedId)
          .createdAt(LocalDateTime.now())
          .build();

      given(feedService.createComment(request, user.getId())).willReturn(response);

      mockMvc.perform(MockMvcRequestBuilders.post("/api/feeds/{feedId}/comments", feedId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.content").value("댓글입니다"));
    }
  }
}
