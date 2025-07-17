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
    long startTime = System.currentTimeMillis();

    // 1. GPT 프롬프트 구성
    String prompt = buildPrompt(objectMapper.writeValueAsString(inputJson));

    // 2. ChatRequest 구성
    ChatRequest.Message message = new ChatRequest.Message("user", prompt, null);
    ChatRequest request = new ChatRequest("gpt-4.1-nano", List.of(message), 0.2);

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

    long endTime = System.currentTimeMillis();
    log.info("gpt 응답 : " + (endTime - startTime) + "ms");

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
            규칙외의 추가적인 추천은 절대 하지 마세요.
            
            추천 규칙:
            1. 각 type(TOP, BOTTOM, SHOES)별 최대 1벌씩만 선택합니다.
               - 예: 상의 1벌을 추천했다면 다른 상의는 추천하지 않고, 하의 1벌을 추천했다면 다른 하의는 추천하지 마세요.
               - 각 타입이 절대 중복되게 추천하지 마세요.
            
            2. 추천하는 하지 않는 옷은 절대 출력하지 마세요.
            
            3. 모든 추천은 입력된 `clothes` 목록 안에서만 선택하세요.
            
            4. `clothesId`, `imageUrl`, `type`, `attributes` 항목값들은 절대 수정하지 마세요.
            
            5. 추천된 옷은 `attributes`의 전체 정보를 포함해야 합니다.
            
            6. 다음 조건에 해당할 경우에만, 추가로 1벌의 옷을 선택하세요:
               - 기온이 20도 이상이면 `DRESS`, 20도 미만이면 `OUTER`
               - 단, 해당 타입의 옷이 입력 목록에 존재할 경우에만 선택 가능합니다.
        
            7. 위 규칙을 제외하고는 **어떠한 추가 추천도 하지 마세요.**
               - type을 변경하거나, LLM이 임의로 판단하여 추천하지 마세요.
            
            8. 마지막으로, 추천한 옷의 이유를 한두 문장으로 설명해 주세요.
            
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
