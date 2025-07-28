package com.example.ootd.domain.clothes.mapper;

import com.example.ootd.domain.clothes.dto.data.ClothesAttributeWithDefDto;
import com.example.ootd.domain.clothes.dto.data.ClothesDto;
import com.example.ootd.domain.clothes.entity.Clothes;
import com.example.ootd.domain.clothes.entity.ClothesAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClothesMapper {

  @Mapping(source = "user.id", target = "ownerId")
  @Mapping(source = "image.url", target = "imageUrl")
  @Mapping(source = "clothesAttributes", target = "attributes")
  ClothesDto toDto(Clothes clothes);

  @Mapping(source = "clothes.user.id", target = "ownerId")
  @Mapping(source = "clothes.image.url", target = "imageUrl")
  @Mapping(source = "clothesAttributes", target = "attributes")
  ClothesDto toDto(Clothes clothes, List<ClothesAttribute> clothesAttributes);

  default List<ClothesDto> toDto(List<Clothes> clothes,
      Map<UUID, List<ClothesAttribute>> clothesAttributeMap) {

    return clothes.stream()
        .map(c -> {
          List<ClothesAttribute> clothesAttributes = clothesAttributeMap.get(c.getId());

          if (clothesAttributes == null) {
            return toDto(c, new ArrayList<>());
          }

          return toDto(c, clothesAttributeMap.get(c.getId()));
        })
        .collect(Collectors.toList());
  }

  @Mapping(source = "attribute.id", target = "definitionId")
  @Mapping(source = "attribute.name", target = "definitionName")
  @Mapping(source = "attribute.details", target = "selectableValues")
  ClothesAttributeWithDefDto toDefDto(ClothesAttribute clothesAttribute);

  List<ClothesAttributeWithDefDto> toDefDto(List<ClothesAttribute> clothesAttribute);
}
