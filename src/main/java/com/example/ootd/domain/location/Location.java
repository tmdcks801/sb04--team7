package com.example.ootd.domain.location;

import com.example.ootd.converter.StringListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "locations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id;
  private double latitude;
  private double longitude;
  @Column(name = "location_x")
  private int locationX;
  @Column(name = "location_y")
  private int locationY;
  @Convert(converter = StringListConverter.class)
  @Column(columnDefinition = "TEXT") // 길이에 따라 TEXT 사용
  private List<String> locationNames;

  public Location(double latitude, double longitude, int locationX, int locationY,
      List<String> locationNames) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.locationX = locationX;
    this.locationY = locationY;
    this.locationNames = locationNames;
  }
}
