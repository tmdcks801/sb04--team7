package com.example.ootd.domain.llm.dto.response;

import com.example.ootd.domain.llm.dto.request.ChatRequest;
import java.util.List;
import lombok.Builder;

@Builder
public record ChatResponse(
    List<Choice> choices
) {

  @Builder
  public record Choice(
    ChatRequest.Message message
  ) {
  }
}
