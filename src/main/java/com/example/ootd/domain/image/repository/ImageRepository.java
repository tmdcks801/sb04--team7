package com.example.ootd.domain.image.repository;

import com.example.ootd.domain.image.entity.Image;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, UUID> {

}
