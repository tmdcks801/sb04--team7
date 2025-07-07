package com.example.ootd.domain.location.repository;

import com.example.ootd.domain.location.Location;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {

  Location findByLatitudeAndLongitude(double latitude, double longitude);

  Optional<Location> findById(UUID id);

  void deleteById(UUID locationId);
}
