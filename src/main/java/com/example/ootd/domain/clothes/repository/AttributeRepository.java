package com.example.ootd.domain.clothes.repository;

import com.example.ootd.domain.clothes.entity.Attribute;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributeRepository extends JpaRepository<Attribute, UUID>,
    CustomAttributeRepository {

}
