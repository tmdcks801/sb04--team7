package com.example.ootd.domain.weather.repository;

import com.example.ootd.domain.weather.entity.Wheather;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WheatherRepository extends JpaRepository<Wheather, UUID> {

}
