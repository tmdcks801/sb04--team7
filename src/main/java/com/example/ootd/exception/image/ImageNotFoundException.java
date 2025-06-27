package com.example.ootd.exception.image;

import com.example.ootd.exception.ErrorCode;
import java.util.UUID;

public class ImageNotFoundException extends ImageException {

  public ImageNotFoundException() {
    super(ErrorCode.IMAGE_NOT_FOUND);
  }

  public static ImageNotFoundException withId(UUID imageId) {
    ImageNotFoundException exception = new ImageNotFoundException();
    exception.addDetail("imageId", imageId);
    return exception;
  }
}
