package com.example.ootd.domain.message;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ootd.domain.message.entity.Message;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MessageTest {

  @ParameterizedTest(name = "[{index}] first={0}, second={1}")
  @CsvSource({
      "11111111-1111-1111-1111-111111111111, 22222222-2222-2222-2222-222222222222",
      "22222222-2222-2222-2222-222222222222, 11111111-1111-1111-1111-111111111111"
  })
  void makeDmKey_isOrderInsensitive(String first, String second) {
    String k1 = Message.makeDmKey(UUID.fromString(first), UUID.fromString(second));
    String k2 = Message.makeDmKey(UUID.fromString(second), UUID.fromString(first));

    assertThat(k1).isEqualTo(k2);
    String[] split = k1.split("_");
    assertThat(split[0]).isLessThan(split[1]);
  }
}
