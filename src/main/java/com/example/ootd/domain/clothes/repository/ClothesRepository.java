package com.example.ootd.domain.clothes.repository;

import com.example.ootd.domain.clothes.entity.Clothes;
import com.example.ootd.domain.clothes.repository.custom.CustomClothesRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClothesRepository extends JpaRepository<Clothes, UUID>, CustomClothesRepository {

  @Query("SELECT c FROM Clothes c LEFT JOIN FETCH c.clothesAttributes ca LEFT JOIN FETCH ca.attribute LEFT JOIN FETCH c.image WHERE c.user.id = :userId")
  List<Clothes> findByUserId(@Param("userId") UUID userId);
}
