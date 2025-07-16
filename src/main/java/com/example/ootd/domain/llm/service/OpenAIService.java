package com.example.ootd.domain.llm.service;

import com.example.ootd.domain.llm.dto.request.ChatRequest;
import com.example.ootd.domain.llm.dto.request.ChatRequest.Message;
import com.example.ootd.domain.llm.dto.response.ChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIService {

  @Value("${OPENAI_API_KEY}")
  private String apiKey;

  private static final String API_URL = "https://api.openai.com/v1/chat/completions";

  private final ObjectMapper objectMapper = new ObjectMapper();

  public String getRecommendation(Map<String, Object> inputJson) throws Exception {

    // 1. GPT 프롬프트 구성
    String prompt = buildPrompt(objectMapper.writeValueAsString(inputJson));

    // 2. ChatRequest 구성
    ChatRequest.Message message = new ChatRequest.Message("user", prompt, null);
    ChatRequest request = new ChatRequest("gpt-4o-mini", List.of(message), 0.7);

    // 3. HTTP 요청 구성
    HttpHeaders headers = new org.springframework.http.HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(apiKey);
    HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);

    // 4. 요청 전송
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> response = restTemplate.exchange(
        API_URL,
        HttpMethod.POST,
        entity,
        String.class
    );

    // 5. 응답 파싱
    ChatResponse chatResponse = objectMapper.readValue(response.getBody(), ChatResponse.class);
    String aiResponse = chatResponse.choices().get(0).message().content();
    log.debug("OpenAI 응답: {}", aiResponse);
    return aiResponse;
  }

  private String buildPrompt(String jsonInput) {
    return """
            다음은 날씨, 사용자 옷장 정보입니다.
            옷장은 JSON의 clothes 항목에 있습니다.
            해당 정보에 맞게 의상을 추천해 주세요.
            
            추천 규칙:
            1. 각 타입(TOP, BOTTOM, SHOES)별로 최대 1개씩만 선택하세요
            2. 당일 기온이 20도 이상이면 DRESS, 20도 미만이면 OUTER 최대 1개씩만 추천하세요
            3. TOP, BOTTOM, SHOES, DRESS, OUTER 제외한 타입들은 랜덤으로 최대 1개씩만 추천하세요
            4. 같은 타입의 옷(TOP, BOTTOM, SHOES, DRESS, OUTER)을 중복으로 추천하지 마세요
            5. 반드시 입력된 clothes 목록에서만 선택하세요
            6. clothesId, imageUrl, attributes는 입력된 옷의 실제 정보를 그대로 사용하세요
            7. 옷의 모든 attributes를 보여주세요
            8. 추천한 이유를 알려주세요

            결과는 반드시 다음과 같은 JSON 구조로 출력하세요:
            {
                "clothes": [
                    {
                        "clothesId": "실제 clothesId 사용",
                        "name": "실제 name 사용", 
                        "imageUrl": "실제 imageUrl 사용",
                        "type": "실제 type 사용",
                        "attributes": [
                            {
                                "definitionId": "cbac78f3-826b-47e8-8e09-8a4d9b1518a8",
                                "definitionName": "두께감",
                                "selectableValues": [
                                    "얇음",
                                    "조금 얇음", 
                                    "조금 두꺼움",
                                    "두꺼움"
                                ],
                                "value": "얇음"
                            }
                        ]
                    }
                ]
            }
            
            추천한 이유 :
            입력 JSON:
            """ + jsonInput;
  }
}
