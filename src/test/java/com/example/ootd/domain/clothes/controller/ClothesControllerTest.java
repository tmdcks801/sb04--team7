package com.example.ootd.domain.clothes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ootd.TestPrincipalUser;
import com.example.ootd.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.example.ootd.domain.clothes.dto.data.ClothesDto;
import com.example.ootd.domain.clothes.dto.request.ClothesCreateRequest;
import com.example.ootd.domain.clothes.dto.request.ClothesUpdateRequest;
import com.example.ootd.domain.clothes.entity.ClothesType;
import com.example.ootd.domain.clothes.service.ClothesService;
import com.example.ootd.domain.sse.service.SsePushServiceInterface;
import com.example.ootd.domain.user.User;
import com.example.ootd.dto.PageResponse;
import com.example.ootd.security.PrincipalUser;
import com.example.ootd.security.jwt.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClothesController.class)
public class ClothesControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockitoBean
  ClothesService clothesService;
  @MockitoBean
  private SsePushServiceInterface ssePushService;
  @MockitoBean
  private JwtService jwtService;
  @MockitoBean
  private AuthenticationManager authenticationManager;
  @MockitoBean
  private UserDetailsService userDetailsService;

  @BeforeEach
  void setup() {
    UUID userId = UUID.randomUUID();
    User mockUser = Mockito.mock(User.class);
    given(mockUser.getId()).willReturn(userId);

    PrincipalUser principalUser = new TestPrincipalUser(mockUser);

    Authentication authentication = new UsernamePasswordAuthenticationToken(
        principalUser,
        null,
        principalUser.getAuthorities()
    );
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }


  @Test
  @DisplayName("GET /api/clothes - 의상 목록 조회 성공")
  void findClothes_success() throws Exception {
    UUID clothesId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID defId = UUID.randomUUID();

    ClothesAttributeWithDefDto attr = ClothesAttributeWithDefDto.builder()
        .definitionId(defId)
        .definitionName("색상")
        .selectableValues(List.of("블랙", "화이트"))
        .value("블랙")
        .build();

    ClothesDto dto = new ClothesDto(
        clothesId,
        userId,
        "반팔 티셔츠",
        "https://example.com/image.jpg",
        ClothesType.TOP,
        List.of(attr)
    );

    PageResponse<ClothesDto> response = PageResponse.<ClothesDto>builder()
        .data(List.of(dto))
        .hasNext(false)
        .nextCursor("cursor123")
        .nextIdAfter(UUID.randomUUID())
        .sortBy("createdAt")
        .sortDirection("ASCENDING")
        .totalCount(1L)
        .build();

    Mockito.when(clothesService.findByCondition(any()))
        .thenReturn(response);

    mockMvc.perform(get("/api/clothes")
            .param("limit", "10")
            .param("page", "1")
            .param("ownerId", userId.toString())
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].name").value("반팔 티셔츠"))
        .andExpect(jsonPath("$.data[0].type").value("TOP"))
        .andExpect(jsonPath("$.data[0].attributes[0].definitionName").value("색상"))
        .andExpect(jsonPath("$.totalCount").value(1));
  }

  @Test
  @DisplayName("POST /api/clothes - 의상 등록 성공")
  void createClothes_success() throws Exception {
    UUID clothesId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    ClothesCreateRequest request = new ClothesCreateRequest(
        userId, "반팔", ClothesType.TOP, List.of()
    );

    ClothesDto dto = new ClothesDto(
        clothesId,
        userId,
        request.name(),
        "https://example.com/image.jpg",
        request.type(),
        List.of()
    );

    MockMultipartFile req = new MockMultipartFile(
        "request", "", "application/json", objectMapper.writeValueAsBytes(request)
    );

    MockMultipartFile image = new MockMultipartFile(
        "image", "shirt.jpg", "image/jpeg", "imagecontent".getBytes()
    );

    given(clothesService.create(any(), any(), any()))
        .willReturn(dto);

    mockMvc.perform(multipart("/api/clothes")
            .file(req)
            .file(image)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .param("ownerId", userId.toString())
            .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("반팔"))
        .andExpect(jsonPath("$.type").value("TOP"));
  }

  @Test
  @DisplayName("PATCH /api/clothes/{id} - 의상 수정 성공")
  void updateClothes_success() throws Exception {
    UUID clothesId = UUID.randomUUID();
    ClothesUpdateRequest request = new ClothesUpdateRequest(
        "긴팔", ClothesType.TOP, List.of()
    );

    ClothesDto dto = new ClothesDto(
        clothesId,
        UUID.randomUUID(),
        request.name(),
        "https://example.com/updated.jpg",
        request.type(),
        List.of()
    );

    MockMultipartFile req = new MockMultipartFile(
        "request", "", "application/json", objectMapper.writeValueAsBytes(request)
    );

    MockMultipartFile image = new MockMultipartFile(
        "image", "new.jpg", "image/jpeg", "updated-image".getBytes()
    );

    Mockito.when(clothesService.update(any(), any(), eq(clothesId)))
        .thenReturn(dto);

    mockMvc.perform(multipart("/api/clothes/" + clothesId)
            .file(req)
            .file(image)
            .with(r -> {
              r.setMethod("PATCH");
              return r;
            })
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("긴팔"));
  }

  @Test
  @DisplayName("DELETE /api/clothes/{id} - 의상 삭제 성공")
  void deleteClothes_success() throws Exception {
    UUID clothesId = UUID.randomUUID();

    mockMvc.perform(delete("/api/clothes/" + clothesId)
            .with(csrf()))
        .andExpect(status().isNoContent());
  }
}
