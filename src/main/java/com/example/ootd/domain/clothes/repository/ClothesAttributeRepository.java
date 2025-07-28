package com.example.ootd.domain.clothes.repository;

import com.example.ootd.domain.clothes.entity.ClothesAttribute;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothesAttributeRepository extends JpaRepository<ClothesAttribute, UUID> {

  void deleteByAttributeId(UUID attributeId);
}
