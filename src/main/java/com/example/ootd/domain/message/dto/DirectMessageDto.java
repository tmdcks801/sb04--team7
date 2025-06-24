package com.example.ootd.domain.message.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class DirectMessageDto {

  UUID id;
  LocalDateTime createAt;
  Sender sender;
  Receiver receiver;
  String content;

}
