package com.example.ootd.domain.user;

import com.example.ootd.security.Provider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

//  @OneToOne
//  @JoinColumn(name = "image_id", nullable = true)
//  private Image image;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  @Column
  private String password;

  @Column
  @Enumerated(EnumType.STRING)
  private UserRole role;

  @Column
  private boolean isLocked;

  @Column
  @Enumerated(EnumType.STRING)
  private Provider provider;

  @Column
  private String providerId;

  @Column
  private LocalDateTime createdAt;

  @Column
  private LocalDateTime updatedAt;

  @Column
  private Gender gender;

  @Column
  private LocalDate birthDate;

  @Column
  private int temperatureSensitivity;


}
