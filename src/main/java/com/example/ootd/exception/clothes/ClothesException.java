package com.example.ootd.exception.clothes;

import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.OotdException;

public class ClothesException extends OotdException {

  public ClothesException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ClothesException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
