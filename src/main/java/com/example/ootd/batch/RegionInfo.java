package com.example.ootd.batch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionInfo {

  private String regionName;
  private int nx;
  private int ny;
}