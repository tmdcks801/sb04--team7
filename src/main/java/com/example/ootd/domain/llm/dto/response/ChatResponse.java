package com.example.ootd.domain.llm.dto.response;

import com.example.ootd.domain.llm.dto.request.ChatRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatResponse(
    List<Choice> choices
) {

  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Choice(
    Integer index,
    ChatRequest.Message message
  ) {
  }
}
