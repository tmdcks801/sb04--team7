package com.example.ootd.domain.feed.mapper;

import com.example.ootd.domain.clothes.mapper.OotdMapper;
import com.example.ootd.domain.feed.dto.data.FeedDto;
import com.example.ootd.domain.feed.entity.Feed;
import com.example.ootd.domain.feed.entity.FeedLike;
import com.example.ootd.domain.user.mapper.UserMapper;
import com.example.ootd.domain.weather.mapper.WeatherSummaryMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, WeatherSummaryMapper.class,
    OotdMapper.class})
public interface FeedMapper {

  @Mapping(target = "author", source = "feed.user")
  @Mapping(target = "weather", source = "feed.weather")
  @Mapping(target = "ootds", source = "feed.feedClothes")
  @Mapping(target = "likedByMe", source = "likedByMe")
  FeedDto toDto(Feed feed, boolean likedByMe);

  @Mapping(target = "author", source = "feed.user")
  @Mapping(target = "weather", source = "feed.weather")
  @Mapping(target = "ootds", source = "feed.feedClothes")
  FeedDto toDto(Feed feed);

  List<FeedDto> toDto(List<Feed> feeds);

  default List<FeedDto> toDto(List<Feed> feeds, Map<UUID, FeedLike> likedByMeMap) {
    return feeds.stream()
        .map(feed -> toDto(feed, likedByMeMap.containsKey(feed.getId())))
        .toList();
  }
}
