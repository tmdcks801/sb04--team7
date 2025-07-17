package com.example.ootd.exception.feed;

import com.example.ootd.exception.ErrorCode;
import java.util.UUID;

public class FeedLikeAlreadyExistsException extends FeedException {

  public FeedLikeAlreadyExistsException() {
    super(ErrorCode.FEED_LIKE_DUPLICATE);
  }

  public static FeedLikeAlreadyExistsException withFeedIdAndUserId(UUID feedId, UUID userId) {
    FeedLikeAlreadyExistsException exception = new FeedLikeAlreadyExistsException();
    exception.addDetail("feedId", feedId);
    exception.addDetail("userId", userId);
    return exception;
  }
}
