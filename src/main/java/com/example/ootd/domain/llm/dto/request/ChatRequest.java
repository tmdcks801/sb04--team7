package com.example.ootd.domain.llm.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Builder;

@Builder
public record ChatRequest(
    String model,
    List<Message> messages,
    double temperature
) {

  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Message(
      String role,       // "system", "user", "assistant"
      String content,    // 프롬프트 내용
      String refusal     // OpenAI API refusal 필드
  ) {
  }
}
