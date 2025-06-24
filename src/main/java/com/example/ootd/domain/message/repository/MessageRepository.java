package com.example.ootd.domain.message.repository;

import com.example.ootd.domain.message.entity.Message;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MessageRepository extends JpaRepository<Message, UUID> {

}
