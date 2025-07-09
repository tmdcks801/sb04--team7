package com.example.ootd.config;

import com.example.ootd.domain.image.service.S3Service;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestConfiguration
public class TestConfig {

  @MockitoBean
  private S3Service s3Service;

  @Bean
  public JPAQueryFactory jpaQueryFactory(@Autowired EntityManager entityManager) {
    return new JPAQueryFactory(entityManager);
  }
}
