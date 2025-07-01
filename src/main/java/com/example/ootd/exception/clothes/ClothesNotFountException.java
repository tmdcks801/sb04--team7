package com.example.ootd.exception.clothes;

import com.example.ootd.exception.ErrorCode;
import java.util.UUID;

public class ClothesNotFountException extends ClothesException {

  public ClothesNotFountException() {
    super(ErrorCode.CLOTHES_NOT_FOUND);
  }

  public static ClothesNotFountException withId(UUID clothesId) {
    ClothesNotFountException exception = new ClothesNotFountException();
    exception.addDetail("clothesId", clothesId);
    return exception;
  }
}
