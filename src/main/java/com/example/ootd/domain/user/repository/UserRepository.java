package com.example.ootd.domain.user.repository;

import com.example.ootd.domain.user.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByEmail(String email);


  @Query("select u.id from User u "
      + "where (:lastId is null or u.id > :lastId) "
      + "order by u.id asc")
  List<UUID> findIdsAfter(@Param("lastId") UUID lastId, Pageable pageable);
}
