package com.example.ootd.exception.feed;

import com.example.ootd.exception.ErrorCode;
import java.util.UUID;

public class FeedLikeNotFoundException extends FeedException {

  public FeedLikeNotFoundException() {
    super(ErrorCode.FEED_LIKE_NOT_FOUND);
  }

  public static FeedLikeNotFoundException withFeedIdAndUserId(UUID feedId, UUID userId) {
    FeedLikeNotFoundException exception = new FeedLikeNotFoundException();
    exception.addDetail("feedId", feedId);
    exception.addDetail("userId", userId);
    return exception;
  }
}
