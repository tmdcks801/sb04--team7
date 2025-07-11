package com.example.ootd.exception.clothes;

import com.example.ootd.exception.ErrorCode;

public class AttributeNameAlreadyExistsException extends ClothesException {

  public AttributeNameAlreadyExistsException() {
    super(ErrorCode.ATTRIBUTE_NAME_DUPLICATE);
  }

  public static AttributeNameAlreadyExistsException withName(String attributeName) {
    AttributeNameAlreadyExistsException exception = new AttributeNameAlreadyExistsException();
    exception.addDetail("attributeName", attributeName);
    return exception;
  }
}
