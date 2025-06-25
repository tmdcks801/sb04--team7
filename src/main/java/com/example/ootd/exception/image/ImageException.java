package com.example.ootd.exception.image;

import com.example.ootd.exception.ErrorCode;
import com.example.ootd.exception.OotdException;

public class ImageException extends OotdException {

  public ImageException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ImageException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
