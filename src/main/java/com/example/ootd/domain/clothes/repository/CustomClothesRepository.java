package com.example.ootd.domain.clothes.repository;

import com.example.ootd.domain.clothes.dto.request.ClothesSearchCondition;
import com.example.ootd.domain.clothes.entity.Clothes;
import com.example.ootd.domain.clothes.entity.ClothesType;
import java.util.List;
import java.util.UUID;

public interface CustomClothesRepository {

  // 조건에 해당하는 옷 검색
  List<Clothes> findByCondition(ClothesSearchCondition condition);

  // 조건에 해당하는 옷 개수
  long countByCondition(ClothesType typeEqual, UUID ownerId);
}
