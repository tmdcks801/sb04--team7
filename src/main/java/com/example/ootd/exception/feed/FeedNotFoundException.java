package com.example.ootd.exception.feed;

import com.example.ootd.exception.ErrorCode;
import java.util.UUID;

public class FeedNotFoundException extends FeedException {

  public FeedNotFoundException() {
    super(ErrorCode.FEED_NOT_FOUND);
  }

  public static FeedNotFoundException withId(UUID feedId) {
    FeedNotFoundException exception = new FeedNotFoundException();
    exception.addDetail("feedId", feedId);
    return exception;
    
  }
}
