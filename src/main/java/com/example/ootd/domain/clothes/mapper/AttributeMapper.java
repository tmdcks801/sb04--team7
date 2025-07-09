package com.example.ootd.domain.clothes.mapper;

import com.example.ootd.domain.clothes.dto.data.ClothesAttributeDefDto;
import com.example.ootd.domain.clothes.entity.Attribute;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttributeMapper {

  @Mapping(source = "attribute.details", target = "selectableValues")
  ClothesAttributeDefDto toDto(Attribute attribute);

  @Mapping(source = "attribute.details", target = "selectableValues")
  List<ClothesAttributeDefDto> toDtoList(List<Attribute> attributes);
}
