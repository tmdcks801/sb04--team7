package com.example.ootd.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegionInfo implements java.io.Serializable {

  private static final long serialVersionUID = 1L;

  private String regionName;
  private int nx;
  private int ny;

  public void setRegionName(String regionName) {
    this.regionName = regionName;
  }

  public void setNx(int nx) {
    this.nx = nx;
  }

  public void setNy(int ny) {
    this.ny = ny;
  }
}