package com.example.ootd.domain.location.repository;

import com.example.ootd.domain.location.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {

  Location findByLatitudeAndLongitude(double latitude, double longitude);
}
