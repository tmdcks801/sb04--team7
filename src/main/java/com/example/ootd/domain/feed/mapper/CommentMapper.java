package com.example.ootd.domain.feed.mapper;

import com.example.ootd.domain.feed.dto.data.CommentDto;
import com.example.ootd.domain.feed.entity.FeedComment;
import com.example.ootd.domain.user.mapper.UserMapper;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {

  @Mapping(target = "author", source = "user")
  @Mapping(target = "feedId", source = "feed.id")
  CommentDto toDto(FeedComment feedComment);

  List<CommentDto> toDto(List<FeedComment> feedComments);
}
