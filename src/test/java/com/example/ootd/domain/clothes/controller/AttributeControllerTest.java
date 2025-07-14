package com.example.ootd.domain.clothes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ootd.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeDefCreateRequest;
import com.example.ootd.domain.clothes.dto.request.ClothesAttributeDefUpdateRequest;
import com.example.ootd.domain.clothes.service.AttributeService;
import com.example.ootd.domain.sse.service.SsePushServiceInterface;
import com.example.ootd.dto.PageResponse;
import com.example.ootd.security.jwt.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AttributeController.class)
class AttributeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private AttributeService attributeService;
  @MockitoBean
  private SsePushServiceInterface ssePushService;
  @MockitoBean
  private JwtService jwtService;
  @MockitoBean
  private AuthenticationManager authenticationManager;
  @MockitoBean
  private UserDetailsService userDetailsService;


  private final UUID defId = UUID.randomUUID();

  @Nested
  @DisplayName("GET /api/clothes/attribute-defs")
  class FindTest {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("성공 - 속성 정의 목록 조회")
    void find_success() throws Exception {
      PageResponse<ClothesAttributeDefDto> response = PageResponse.<ClothesAttributeDefDto>builder()
          .data(List.of(new ClothesAttributeDefDto(defId, "색상", List.of("빨강", "파랑"))))
          .hasNext(false)
          .totalCount(1)
          .sortBy("createdAt")
          .sortDirection("DESCENDING")
          .build();

      BDDMockito.given(attributeService.findByCondition(any()))
          .willReturn(response);

      mockMvc.perform(get("/api/clothes/attribute-defs")
              .param("limit", "5")
              .param("ownerId", UUID.randomUUID().toString())
              .param("sortBy", "createdAt")
              .param("sortDirection", "DESCENDING")
              .with(csrf()))  // CSRF 토큰 포함
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data[0].name").value("색상"))
          .andExpect(jsonPath("$.data[0].selectableValues[0]").value("빨강"));
    }
  }

  @Nested
  @DisplayName("POST /api/clothes/attribute-defs")
  class Create {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("성공 - 속성 정의 등록")
    void create_success() throws Exception {
      ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest("스타일",
          List.of("캐주얼", "포멀"));
      ClothesAttributeDefDto dto = new ClothesAttributeDefDto(defId, "스타일", List.of("캐주얼", "포멀"));

      BDDMockito.given(attributeService.create(any()))
          .willReturn(dto);

      mockMvc.perform(post("/api/clothes/attribute-defs")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))  // CSRF 토큰 포함
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.name").value("스타일"))
          .andExpect(jsonPath("$.selectableValues[1]").value("포멀"));
    }
  }

  @Nested
  @DisplayName("PATCH /api/clothes/attribute-defs/{id}")
  class UpdateTest {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("성공 - 속성 정의 수정")
    void update_success() throws Exception {
      ClothesAttributeDefUpdateRequest request = new ClothesAttributeDefUpdateRequest("색상",
          List.of("흰색", "검정"));
      ClothesAttributeDefDto responseDto = new ClothesAttributeDefDto(defId, "색상",
          List.of("흰색", "검정"));

      BDDMockito.given(attributeService.update(eq(request), eq(defId)))
          .willReturn(responseDto);

      mockMvc.perform(patch("/api/clothes/attribute-defs/" + defId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))  // CSRF 토큰 포함
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.selectableValues[0]").value("흰색"));
    }
  }

  @Nested
  @DisplayName("DELETE /api/clothes/attribute-defs/{id}")
  class DeleteTest {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("성공 - 속성 정의 삭제")
    void delete_success() throws Exception {
      mockMvc.perform(delete("/api/clothes/attribute-defs/" + defId)
              .with(csrf()))  // CSRF 토큰 포함
          .andExpect(status().isNoContent());
    }
  }
}
