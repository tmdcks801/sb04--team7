package com.example.ootd.exception.clothes;

import com.example.ootd.exception.ErrorCode;
import java.util.UUID;

public class AttributeNotFoundException extends ClothesException {

  public AttributeNotFoundException() {
    super(ErrorCode.ATTRIBUTE_NOT_FOUND);
  }

  public static AttributeNotFoundException withId(UUID attributeId) {
    AttributeNotFoundException exception = new AttributeNotFoundException();
    exception.addDetail("attributeId", attributeId);
    return exception;
  }
}
