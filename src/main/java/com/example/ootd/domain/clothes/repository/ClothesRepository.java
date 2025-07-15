package com.example.ootd.domain.clothes.repository;

import com.example.ootd.domain.clothes.entity.Clothes;
import com.example.ootd.domain.clothes.repository.custom.CustomClothesRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothesRepository extends JpaRepository<Clothes, UUID>, CustomClothesRepository {

  List<Clothes> findByUserId(UUID userId);
}
