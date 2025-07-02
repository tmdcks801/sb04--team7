package com.example.ootd.domain.feed.service.impl;

import com.example.ootd.domain.clothes.entity.Clothes;
import com.example.ootd.domain.clothes.repository.ClothesRepository;
import com.example.ootd.domain.feed.dto.data.CommentDto;
import com.example.ootd.domain.feed.dto.data.FeedDto;
import com.example.ootd.domain.feed.dto.request.CommentCreateRequest;
import com.example.ootd.domain.feed.dto.request.FeedCommentSearchRequest;
import com.example.ootd.domain.feed.dto.request.FeedCreateRequest;
import com.example.ootd.domain.feed.dto.request.FeedSearchCondition;
import com.example.ootd.domain.feed.dto.request.FeedUpdateRequest;
import com.example.ootd.domain.feed.entity.Feed;
import com.example.ootd.domain.feed.entity.FeedClothes;
import com.example.ootd.domain.feed.mapper.FeedMapper;
import com.example.ootd.domain.feed.repository.FeedRepository;
import com.example.ootd.domain.feed.service.FeedService;
import com.example.ootd.domain.user.User;
import com.example.ootd.domain.user.repository.UserRepository;
import com.example.ootd.domain.weather.entity.Weather;
import com.example.ootd.domain.weather.repository.WeatherRepository;
import com.example.ootd.dto.PageResponse;
import com.example.ootd.exception.feed.FeedNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

  private final FeedRepository feedRepository;
  private final UserRepository userRepository;
  private final WeatherRepository weatherRepository;
  private final ClothesRepository clothesRepository;
  private final FeedMapper feedMapper;

  @Override
  public FeedDto createFeed(FeedCreateRequest request) {

    log.debug("피드 등록 시작: {}", request);

    User author = userRepository.findById(request.authorId())
        .orElseThrow(); // TODO: null 예외처리

    Weather weather = weatherRepository.findById(request.weatherId())
        .orElseThrow(); // TODO: null 예외처리

    List<Clothes> clothesList = clothesRepository.findAllById(request.clothesIds());

    Feed feed = Feed.builder()
        .user(author)
        .weather(weather)
        .content(request.content())
        .build();

    for (Clothes clothes : clothesList) {
      FeedClothes feedClothes = FeedClothes.builder()
          .feed(feed)
          .clothes(clothes)
          .build();
      feed.addFeedClothes(feedClothes);
    }

    FeedDto dto = feedMapper.toDto(feed, false);

    log.info("피드 등록 완료: {}", dto);

    return dto;
  }

  @Override
  public FeedDto updateFeed(UUID feedId, FeedUpdateRequest request) {

    log.debug("피드 수정 시작: feedId={}, {}", feedId, request);

    Feed feed = getFeedById(feedId);
    feed.updateContent(request.content());

    // TODO: 좋아요 기능 만들고 likedByMe 수정
    FeedDto dto = feedMapper.toDto(feed, true);

    log.info("피드 수정 완료: ");

    return dto;
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<FeedDto> findFeedByCondition(FeedSearchCondition condition) {

    return null;
  }

  @Override
  public void deleteFeed(UUID feedId) {

    log.debug("피드 삭제 시작: feedId={}", feedId);

    Feed feed = getFeedById(feedId);
    feedRepository.delete(feed);

    log.info("피드 삭제 완료");
  }

  @Override
  public FeedDto likeFeed(UUID feedId) {
    return null;
  }

  @Override
  public FeedDto deleteFeedLike(UUID feedId) {
    return null;
  }

  @Override
  public CommentDto createComment(UUID feedId, CommentCreateRequest request) {
    return null;
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<CommentDto> findCommentByCondition(FeedCommentSearchRequest request) {
    return null;
  }

  private Feed getFeedById(UUID feedId) {
    return feedRepository.findById(feedId)
        .orElseThrow(() -> FeedNotFoundException.withId(feedId));
  }
}
