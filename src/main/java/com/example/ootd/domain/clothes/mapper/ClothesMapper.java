package com.example.ootd.domain.clothes.mapper;

import com.example.ootd.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.example.ootd.domain.clothes.dto.data.ClothesDto;
import com.example.ootd.domain.clothes.entity.Clothes;
import com.example.ootd.domain.clothes.entity.ClothesAttribute;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClothesMapper {

  @Mapping(source = "user.id", target = "ownerId")
  @Mapping(source = "image.url", target = "imageUrl")
  @Mapping(source = "clothesAttributes", target = "attributes")
  ClothesDto toDto(Clothes clothes);

  List<ClothesDto> toDto(List<Clothes> clothes);

  @Mapping(source = "attribute.id", target = "definitionId")
  @Mapping(source = "attribute.name", target = "definitionName")
  @Mapping(source = "attribute.details", target = "selectableValues")
  ClothesAttributeWithDefDto toDefDto(ClothesAttribute clothesAttribute);

  List<ClothesAttributeWithDefDto> toDefDto(List<ClothesAttribute> clothesAttribute);
}
