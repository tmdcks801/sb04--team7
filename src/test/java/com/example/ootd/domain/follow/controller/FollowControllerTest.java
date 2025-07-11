package com.example.ootd.domain.follow.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.example.ootd.domain.follow.dto.FollowCreateRequest;
import com.example.ootd.domain.follow.dto.FollowDto;
import com.example.ootd.domain.follow.dto.FollowListCondition;
import com.example.ootd.domain.follow.dto.FollowListResponse;
import com.example.ootd.domain.follow.dto.FollowSummaryDto;
import com.example.ootd.domain.follow.service.FollowService;
import com.example.ootd.domain.user.dto.UserSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowController 테스트")
class FollowControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FollowService followService;

    @InjectMocks
    private FollowController followController;

    private ObjectMapper objectMapper;

    private UUID followerId;
    private UUID followeeId;
    private UUID followId;
    private FollowDto followDto;
    private FollowSummaryDto followSummaryDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(followController).build();
        objectMapper = new ObjectMapper();
        
        followerId = UUID.randomUUID();
        followeeId = UUID.randomUUID();
        followId = UUID.randomUUID();

        UserSummary followerSummary = new UserSummary(followerId, "팔로워", "profile1.jpg");
        UserSummary followeeSummary = new UserSummary(followeeId, "팔로위", "profile2.jpg");

        followDto = new FollowDto(followId, followeeSummary, followerSummary);
        followSummaryDto = new FollowSummaryDto(followeeId, 5L, 10L, true, followId, false);
    }

    @Test
    @DisplayName("팔로우 생성 성공")
    void createFollowSuccess() throws Exception {
        // given
        FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);
        given(followService.createFollow(any(FollowCreateRequest.class))).willReturn(followDto);

        // when & then
        mockMvc.perform(post("/api/follows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(followId.toString()));
    }

    @Test
    @DisplayName("팔로우 요약 정보 조회 성공")
    void getSummarySuccess() throws Exception {
        // given
        given(followService.getSummaryFollow(followerId)).willReturn(followSummaryDto);

        // when & then
        mockMvc.perform(get("/api/follows/summary")
                .param("userId", followerId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.followerCount").value(5))
            .andExpect(jsonPath("$.followingCount").value(10));
    }

    @Test
    @DisplayName("팔로우 삭제 성공")
    void deleteFollowSuccess() throws Exception {
        // given
        doNothing().when(followService).deleteFollow(followId);

        // when & then
        mockMvc.perform(delete("/api/follows/{followId}", followId))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("팔로우 생성 요청 검증")
    void createFollowValidation() throws Exception {
        // given
        FollowCreateRequest request = new FollowCreateRequest(followerId, followeeId);
        given(followService.createFollow(any())).willReturn(followDto);

        // when & then
        mockMvc.perform(post("/api/follows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("팔로우 요약 조회 파라미터 검증")
    void getSummaryParameterValidation() throws Exception {
        // given
        given(followService.getSummaryFollow(any())).willReturn(followSummaryDto);

        // when & then
        mockMvc.perform(get("/api/follows/summary")
                .param("userId", followerId.toString()))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("팔로워 목록 조회 성공")
    void getFollowerListSuccess() throws Exception {
        // given
        FollowListResponse response = FollowListResponse.builder()
            .data(List.of(followDto))
            .nextCursor(null)
            .nextIdAfter(null)
            .hasNext(false)
            .totalCount(1)
            .sortBy("id")
            .sortDirection(null)
            .build();
        given(followService.getFollowerList(any(FollowListCondition.class), any(UUID.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/follows/followers")
                .param("followeeId", followeeId.toString())
                .param("limit", "10"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("팔로잉 목록 조회 성공")
    void getFollowingListSuccess() throws Exception {
        // given
        FollowListResponse response = FollowListResponse.builder()
            .data(List.of(followDto))
            .nextCursor(null)
            .nextIdAfter(null)
            .hasNext(false)
            .totalCount(1)
            .sortBy("id")
            .sortDirection(null)
            .build();
        given(followService.getFollowingList(any(FollowListCondition.class), any(UUID.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/follows/followings")
                .param("followerId", followerId.toString())
                .param("limit", "10"))
            .andExpect(status().isOk());
    }
}
