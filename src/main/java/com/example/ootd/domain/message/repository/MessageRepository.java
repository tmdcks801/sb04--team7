package com.example.ootd.domain.message.repository;

import com.example.ootd.domain.message.entity.Message;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MessageRepository extends JpaRepository<Message, UUID> {

  List<Message> findByDmKey(String dmKey, Pageable pageable);

  List<Message> findByDmKeyAndIdGreaterThan(String dmKey, UUID cursor, Pageable pageable);

  List<Message> findByDmKeyAndIdLessThan(String dmKey, UUID cursor, Pageable pageable);
}
