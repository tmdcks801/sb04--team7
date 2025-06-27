package com.example.ootd.domain.user;

import com.example.ootd.domain.image.entity.Image;
import com.example.ootd.domain.location.Location;
import com.example.ootd.security.Provider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne
  @JoinColumn(name = "image_id", nullable = true)
  private Image image;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "location_id")
  private Location location;

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
  private Boolean isLocked;

  @Column
  @Enumerated(EnumType.STRING)
  private Provider provider;

  @Column
  private String providerId;

  @Column
  @CreatedDate
  private LocalDateTime createdAt;

  @Column
  @LastModifiedDate
  private LocalDateTime updatedAt;

  @Column
  private Gender gender;

  @Column
  private LocalDate birthDate;

  @Column
  private int temperatureSensitivity;

  public User(String name, String email, String password) {
    this.name = name;
    this.email = email;
    this.password = password;
    this.role = UserRole.ROLE_USER;
    this.isLocked = false;
    this.provider = null;
    this.providerId = null;
    this.gender = Gender.OTHER;
    this.birthDate = LocalDate.now(); // TODO: 변경
    this.temperatureSensitivity = 3;
  }
}
