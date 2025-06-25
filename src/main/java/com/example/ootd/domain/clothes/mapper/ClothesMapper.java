package com.example.ootd.domain.clothes.mapper;

import com.example.ootd.domain.clothes.dto.data.ClothesDto;
import com.example.ootd.domain.clothes.entity.Clothes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// TODO: ClothesDto의 attribute도 변환 필요 -> 해당 기능 구현 시 수정
@Mapper(componentModel = "spring")
public interface ClothesMapper {

  @Mapping(source = "user.id", target = "ownerId")
  @Mapping(source = "image.url", target = "imageUrl")
  ClothesDto toDto(Clothes clothes);
}
