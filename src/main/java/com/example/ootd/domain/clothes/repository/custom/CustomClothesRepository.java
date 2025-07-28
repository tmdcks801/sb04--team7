package com.example.ootd.domain.clothes.repository.custom;

import com.example.ootd.domain.clothes.dto.request.ClothesSearchCondition;
import com.example.ootd.domain.clothes.entity.Clothes;
import com.example.ootd.domain.clothes.entity.ClothesAttribute;
import com.example.ootd.domain.clothes.entity.ClothesType;
import java.util.List;
import java.util.UUID;

public interface CustomClothesRepository {

  // 조건에 해당하는 옷 검색
  List<Clothes> findByCondition(ClothesSearchCondition condition);

  // 해당 옷들의 속성 리스트 반환
  List<ClothesAttribute> findClothesAttributeByClothesIds(List<UUID> clothesIds);

  // 해당 옷들의 속성 리스트 반환
  List<ClothesAttribute> findClothesAttributeByClothesId(UUID clothesId);
    
  // 조건에 해당하는 옷 개수
  long countByCondition(ClothesType typeEqual, UUID ownerId);
}
