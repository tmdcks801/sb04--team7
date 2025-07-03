package com.example.ootd.domain.clothes.mapper;

import com.example.ootd.domain.clothes.dto.data.OotdDto;
import com.example.ootd.domain.feed.entity.FeedClothes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ClothesMapper.class})
public interface OotdMapper {

  @Mapping(target = "clothesId", source = "clothes.id")
  @Mapping(target = "name", source = "clothes.name")
  @Mapping(target = "imageUrl", source = "clothes.image.url")
  @Mapping(target = "type", source = "clothes.type")
  @Mapping(target = "attributes", source = "clothes.clothesAttributes")
  OotdDto toDto(FeedClothes feedClothes);
}
