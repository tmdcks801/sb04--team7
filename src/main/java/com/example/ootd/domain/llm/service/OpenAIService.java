package com.example.ootd.domain.llm.service;

import com.example.ootd.domain.llm.dto.request.ChatRequest;
import com.example.ootd.domain.llm.dto.request.ChatRequest.Message;
import com.example.ootd.domain.llm.dto.response.ChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
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
public class OpenAIService {

  @Value("${OPENAI_API_KEY}")
  private String apiKey;

  private static final String API_URL = "https://api.openai.com/v1/chat/completions";

  private final ObjectMapper objectMapper = new ObjectMapper();

  public String getRecommendation(Map<String, Object> inputJson) throws Exception {

    // 1. GPT 프롬프트 구성
    String prompt = buildPrompt(objectMapper.writeValueAsString(inputJson));

    // 2. ChatRequest 구성
    ChatRequest.Message message = new ChatRequest.Message("user", prompt);
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
    return chatResponse.choices().get(0).message().content();
  }

  private String buildPrompt(String jsonInput) {
    return """
            다음은 날씨, 사용자 옷장 정보입니다.
            옷장은 JSON의 clothes 항목에 있습니다.
            해당 정보에 맞게 의상을 추천해 주세요. 추천은 top, bottom, shoes, dress, outer, accessory 항목으로 구성하고,
            입력된 clothes 목록 안에서 골라야 합니다.

            결과는 반드시 다음과 같은 JSON 구조로 출력하세요:
            {
                "clothes": [
                    {
                        "clothesId": "d6072fc4-2937-418d-b334-aebab15754c2",
                        "name": "나이키",
                        "imageUrl": null,
                        "type": "TOP",
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

            중요: 반드시 입력된 clothes 목록에서만 옷을 선택하세요. clothesId는 입력된 옷의 실제 ID를 사용하고,
            attributes는 해당 옷의 실제 속성 정보를 사용해야 합니다.

            입력 JSON:
            """ + jsonInput;
  }
}
