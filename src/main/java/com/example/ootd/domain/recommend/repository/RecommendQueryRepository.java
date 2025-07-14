package com.example.ootd.domain.recommend.repository;

import com.example.ootd.domain.clothes.entity.Clothes;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendQueryRepository extends JpaRepository<Clothes, UUID> {

  /**
   * 옷 추천
   */
  @Query(value = """
        SELECT 
            c.id, 
            c.name, 
            c.type, 
            i.url as image_url,
            w.temperature_current, 
            w.precipitation_amount, 
            w.humidity_current, 
            w.wind_speed,
            u.temperature_sensitivity,
            thickness_ca.value as thickness,
            color_ca.value as color,
            season_ca.value as season
            
        FROM clothes c
        LEFT JOIN images i ON c.image_id = i.id
        JOIN weather w ON w.id = :weatherId
        JOIN users u ON u.id = :userId
        LEFT JOIN clothes_attributes thickness_ca ON c.id = thickness_ca.clothes_id
        LEFT JOIN attributes thickness_a ON thickness_ca.attribute_id = thickness_a.id AND thickness_a.name = '두께감'
        LEFT JOIN clothes_attributes color_ca ON c.id = color_ca.clothes_id
        LEFT JOIN attributes color_a ON color_ca.attribute_id = color_a.id AND color_a.name = '색상'
        LEFT JOIN clothes_attributes season_ca ON c.id = season_ca.clothes_id
        LEFT JOIN attributes season_a ON season_ca.attribute_id = season_a.id AND season_a.name = '계절'
        WHERE c.user_id = :userId
        ORDER BY c.type, c.name
        """, nativeQuery = true)
    List<Object[]> findClothesRecommendations(@Param("weatherId") UUID weatherId,
                                              @Param("userId") UUID userId);
}
