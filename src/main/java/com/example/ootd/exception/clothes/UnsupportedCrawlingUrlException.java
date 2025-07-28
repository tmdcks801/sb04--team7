package com.example.ootd.exception.clothes;

import com.example.ootd.exception.ErrorCode;

public class UnsupportedCrawlingUrlException extends ClothesException {

  public UnsupportedCrawlingUrlException() {
    super(ErrorCode.UNSUPPORTED_CRAWLING_URL);
  }

  public static UnsupportedCrawlingUrlException withUrl(String url) {
    UnsupportedCrawlingUrlException exception = new UnsupportedCrawlingUrlException();
    exception.addDetail("url", url);
    return exception;
  }
}
