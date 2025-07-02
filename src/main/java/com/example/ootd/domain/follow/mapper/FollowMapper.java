package com.example.ootd.domain.follow.mapper;

import com.example.ootd.domain.follow.dto.FollowDto;
import com.example.ootd.domain.follow.entity.Follow;
import com.example.ootd.domain.user.dto.UserSummary;
import com.example.ootd.domain.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FollowMapper {

    @Mapping(source = "follow.id", target = "id")
    @Mapping(source = "follow.followee", target = "followee")
    @Mapping(source = "follow.follower", target = "follower")
    FollowDto toDto(Follow follow);

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "image.url", target = "profileImageUrl")
    UserSummary toUserSummary(User user);
}
