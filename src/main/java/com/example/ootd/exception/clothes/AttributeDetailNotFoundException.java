package com.example.ootd.exception.clothes;

import com.example.ootd.exception.ErrorCode;

public class AttributeDetailNotFoundException extends ClothesException {

  public AttributeDetailNotFoundException() {
    super(ErrorCode.ATTRIBUTE_DETAIL_NOT_FOUND);
  }

  public static AttributeDetailNotFoundException withValue(String value) {
    AttributeDetailNotFoundException exception = new AttributeDetailNotFoundException();
    exception.addDetail("value", value);
    return exception;
  }
}
