package com.example.ootd.domain.recommend.controller;

import com.example.ootd.domain.clothes.entity.ClothesType;
import com.example.ootd.domain.recommend.dto.RecommendClothesDto;
import com.example.ootd.domain.recommend.dto.RecommendationDto;
import com.example.ootd.domain.recommend.service.RecommendService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RecommendControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RecommendService recommendService;

    @InjectMocks
    private RecommendController recommendController;

    @Test
    @DisplayName("옷 추천 API 테스트")
    void recommendClothes() throws Exception {
        // Given
        mockMvc = MockMvcBuilders.standaloneSetup(recommendController).build();

        UUID weatherId = UUID.randomUUID();
        RecommendationDto mockRecommendation = RecommendationDto.builder()
                .weatherId(weatherId)
                .userId(UUID.randomUUID())
                .clothes(List.of(
                        RecommendClothesDto.builder().clothesId(UUID.randomUUID()).name("티셔츠").type(ClothesType.TOP).imageUrl("top.jpg").build(),
                        RecommendClothesDto.builder().clothesId(UUID.randomUUID()).name("청바지").type(ClothesType.BOTTOM).imageUrl("bottom.jpg").build(),
                        RecommendClothesDto.builder().clothesId(UUID.randomUUID()).name("운동화").type(ClothesType.SHOES).imageUrl("shoes.jpg").build()
                ))
                .build();

        when(recommendService.recommend(any(UUID.class))).thenReturn(mockRecommendation);

        // When & Then
        mockMvc.perform(get("/api/recommendations")
                        .param("weatherId", weatherId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clothes[0].name").value("티셔츠"))
                .andExpect(jsonPath("$.clothes[1].name").value("청바지"))
                .andExpect(jsonPath("$.clothes[2].name").value("운동화"));
    }
}