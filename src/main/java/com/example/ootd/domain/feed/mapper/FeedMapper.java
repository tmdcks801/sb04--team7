package com.example.ootd.domain.feed.mapper;

import com.example.ootd.domain.clothes.mapper.OotdMapper;
import com.example.ootd.domain.feed.dto.data.FeedDto;
import com.example.ootd.domain.feed.entity.Feed;
import com.example.ootd.domain.user.mapper.UserMapper;
import com.example.ootd.domain.weather.mapper.WeatherSummaryMapper;
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
}
