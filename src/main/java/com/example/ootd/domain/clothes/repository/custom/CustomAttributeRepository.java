package com.example.ootd.domain.clothes.repository.custom;

import com.example.ootd.domain.clothes.dto.request.ClothesAttributeSearchCondition;
import com.example.ootd.domain.clothes.entity.Attribute;
import java.util.List;

public interface CustomAttributeRepository {

  // 조건에 해당하는 속성 검색
  List<Attribute> findByCondition(ClothesAttributeSearchCondition condition);

  // 조건에 해당하는 속성 개수
  long countByKeyword(String keywordLike);
}
